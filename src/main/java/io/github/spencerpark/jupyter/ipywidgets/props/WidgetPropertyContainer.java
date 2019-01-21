package io.github.spencerpark.jupyter.ipywidgets.props;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetComm;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetState;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO should be an interface as we intend to create a declarative version.
public class WidgetPropertyContainer implements WidgetState, Closeable {
    private static final Map<WidgetCoordinates, Supplier<? extends WidgetPropertyContainer>> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<UUID, WeakReference<WidgetPropertyContainer>> INSTANCES = new ConcurrentHashMap<>();

    protected static WidgetCoordinates register(Supplier<? extends WidgetPropertyContainer> instantiator, WidgetCoordinates coords) {
        REGISTRY.put(coords, instantiator);
        return coords;
    }

    public static <T extends WidgetPropertyContainer> T instantiate(WidgetCoordinates coords) {
        Supplier<? extends WidgetPropertyContainer> instantiator = REGISTRY.get(coords);
        // TODO support multiple versions
        return instantiator == null ? null : (T) instantiator.get();
    }

    public static WidgetPropertyContainer lookupInstance(UUID id) {
        WeakReference<WidgetPropertyContainer> ref = INSTANCES.get(id);
        if (ref == null)
            return null;

        return ref.get();
    }

    private final UUID id = UUID.randomUUID();

    protected WidgetPropertyContainer enclosingContainer;
    private final Map<String, WidgetProperty> props = new LinkedHashMap<>();
    private final Map<String, WidgetPropertyContainer> inlineContainers = new LinkedHashMap<>();
    private final Set<String> subWidgetProps = new LinkedHashSet<>();

    private RemoteWidgetState remote = null;

    public WidgetPropertyContainer() {
        INSTANCES.put(this.id, new WeakReference<>(this));
    }

    public UUID getId() {
        return this.id;
    }

    // Clean up the instance from the INSTANCES map, this map holds a weak reference to the instance and should
    // therefore not prevent GC from claiming this object but we must also remove the id and reference object from the
    // map as well to avoid leaking it.
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        INSTANCES.remove(this.id);
    }

    public boolean isOpen() {
        return this.remote != null;
    }

    public boolean isClosed() {
        return !this.isOpen();
    }

    public boolean isConnected() {
        return this.isOpen() && !this.remote.isAccessible();
    }

    public RemoteWidgetState open(CommManager commManager) {
        if (this.isConnected())
            return this.remote;

        WidgetComm comm = commManager.openComm("jupyter.widget", (manager, id, target, openMsg) -> {
            openMsg.getMetadata().put("version", "2.0.0");

            WidgetComm.initializeOpenMessage(openMsg, this.constructPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL)));

            return new WidgetComm(manager, id, target, this);
        });

        this.remote = comm;

        return comm;
    }

    @Override
    public void close() {
        if (this.isOpen()) {
            this.remote.close();
            this.remote = null;
        }
    }

    public void sync() {
        if (this.isOpen()) {
            StatePatch patch = this.constructPatch();
            this.remote.updateState(patch);
        }
    }

    public void replaceRemote(RemoteWidgetState remote) {
        this.close(); // TODO is this method necessary?
        this.remote = remote;
    }

    // TODO check for duplicated names?

    protected <P extends WidgetProperty> P registerProperty(String name, P property) {
        this.props.put(name, property);
        return property;
    }

    protected <T> WidgetProperty<T> property(String name, Type type) {
        return this.registerProperty(name, new SimpleProperty<>(type, null));
    }

    protected <T> WidgetProperty<T> property(String name, Type type, T defaultValue) {
        return this.registerProperty(name, new SimpleProperty<>(type, defaultValue));
    }

    protected <T> WidgetProperty<T> property(String name, T defaultValue) {
        return this.registerProperty(name, new SimpleProperty<>(defaultValue.getClass(), defaultValue));
    }

    protected <T extends WidgetPropertyContainer> T inline(String prefix, T childContainer) {
        if (childContainer.enclosingContainer != null)
            throw new IllegalStateException("Container is already inlined in another container.");

        childContainer.enclosingContainer = this;
        this.inlineContainers.put(prefix, childContainer);

        return childContainer;
    }

    protected <T extends WidgetPropertyContainer> T inline(String prefix, T childContainer, Consumer<T> configure) {
        configure.accept(childContainer);
        return this.inline(prefix, childContainer);
    }


    public StatePatch createPatch(EnumSet<StatePatch.Opts> opts) {
        Gson gson = WidgetsGson.getThreadLocalInstance();

        StatePatch patch = new StatePatch();
        this.populatePatch("", gson, patch, opts);

        return patch;
    }

    private void populatePatch(String prefix, Gson gson, StatePatch patch, EnumSet<StatePatch.Opts> opts) {
        this.inlineContainers.forEach((name, child) ->
                child.populatePatch(prefix + name, gson, patch, opts));

        boolean all = opts.contains(StatePatch.Opts.INCLUDE_ALL);
        boolean clearDirty = opts.contains(StatePatch.Opts.CLEAR_DIRTY);
        this.props.forEach((name, prop) -> {
            if (all || prop.isDirty()) {
                if (prop instanceof RawDataWidgetProperty)
                    patch.putBinary(prefix + name, ((RawDataWidgetProperty) prop).toBytes());
                else if (prop instanceof MultiRawDataWidgetProperty)
                    patch.putBinary(prefix + name, ((MultiRawDataWidgetProperty) prop).toBytes());
                else
                    patch.putJson(prefix + name, gson.toJsonTree(prop.get(), prop.getType()));

                if (clearDirty)
                    prop.setDirty(false);
            }
        });
    }

    public void applyPatch(StatePatch patch) {
        Gson gson = WidgetsGson.getThreadLocalInstance();

        this.extractPatch("", gson, patch);
    }

    private void extractPatch(String prefix, Gson gson, StatePatch patch) {
        // Let the inlineContainers take what they need from the patch.
        // TODO this will iterate over the entire patch for every inlined container
        this.inlineContainers.forEach((name, child) ->
                child.extractPatch(prefix + name, gson, patch));

        patch.forEachJson((key, json) -> {
            if (!key.startsWith(prefix))
                return;
            key = key.substring(prefix.length());

            WidgetProperty prop = this.props.get(key);
            if (prop == null)
                return;

            prop.set(gson.fromJson(json, prop.getType()));
            prop.setDirty(false);
        });

        patch.forEachBuffer((key, buffers) -> {
            if (!key.startsWith(prefix))
                return;
            key = key.substring(prefix.length());

            WidgetProperty prop = this.props.get(key);
            if (prop == null)
                return;

            if (prop instanceof RawDataWidgetProperty) {
                ((RawDataWidgetProperty) prop).fromBytes(buffers.get(0));
                prop.setDirty(false);
            } else if (prop instanceof MultiRawDataWidgetProperty) {
                ((MultiRawDataWidgetProperty) prop).fromBytes(buffers);
                prop.setDirty(false);
            }
        });
    }

    @Override
    public void handleCustomContent(JsonElement payload) {
        // TODO implement event listener style attachments for custom content?
        return
    }

    public void update(Runnable updater) {
        try {
            this.pauseSync();
            updater.run();
        } finally {
            this.resumeSync();
            this.sync();
        }
    }
}

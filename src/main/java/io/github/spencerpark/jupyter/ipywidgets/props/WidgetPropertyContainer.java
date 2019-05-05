package io.github.spencerpark.jupyter.ipywidgets.props;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.ipywidgets.protocol.*;
import io.github.spencerpark.jupyter.kernel.display.DisplayDataRenderable;
import io.github.spencerpark.jupyter.kernel.display.RenderContext;
import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;

import java.io.Closeable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// TODO should be an interface as we intend to create a declarative version.
public class WidgetPropertyContainer implements DisplayDataRenderable, WidgetState, Closeable {
    public static final MIMEType MIME_TYPE = MIMEType.parse("application/vnd.jupyter.widget-view+json");

    private static final MethodType WPC_CONSTRUCTOR_SIGNATURE = MethodType.methodType(void.class, WidgetContext.class);

    private static final class ReflectWidgetPropertyContainerConstructor<T extends WidgetPropertyContainer> implements WidgetPropertyContainerConstructor<T> {
        private final MethodHandle handle;

        public ReflectWidgetPropertyContainerConstructor(MethodHandle handle) {
            this.handle = handle;
        }

        @Override
        public T construct(WidgetContext context) {
            try {
                return (T) handle.invokeExact(context);
            } catch (Throwable t) {
                throw new RuntimeException("Error invoking constructor", t);
            }
        }
    }

    private static final Map<WidgetCoordinates, WidgetPropertyContainerConstructor<?>> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Class, WidgetCoordinates> INV_REGISTRY = new ConcurrentHashMap<>();

    protected static <T extends WidgetPropertyContainer> WidgetCoordinates register(Class<? extends T> type, WidgetPropertyContainerConstructor<T> constructor, WidgetCoordinates coords) {
        REGISTRY.put(coords, constructor);
        INV_REGISTRY.put(type, coords);
        return coords;
    }

    protected static <T extends WidgetPropertyContainer> WidgetCoordinates register(Class<? extends T> type, WidgetCoordinates coords) {
        MethodHandle constructor;
        try {
            constructor = MethodHandles.publicLookup().findConstructor(type, WPC_CONSTRUCTOR_SIGNATURE);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Widget classes registered without a builder function must have a public constructor that accepts a WidgetContext.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        REGISTRY.put(coords, new ReflectWidgetPropertyContainerConstructor<>(constructor));
        INV_REGISTRY.put(type, coords);
        return coords;
    }

    public static <T extends WidgetPropertyContainer> T instantiate(WidgetCoordinates coords, WidgetContext context) {
        WidgetPropertyContainerConstructor<?> constructor = REGISTRY.get(coords);
        // TODO support multiple versions
        return constructor == null ? null : (T) constructor.construct(context);
    }

    protected WidgetPropertyContainer enclosingContainer;
    private final Map<String, WidgetProperty> props = new LinkedHashMap<>();
    private final Map<String, WidgetPropertyContainer> inlineContainers = new LinkedHashMap<>();

    // Isolated props are accessible through this container as a regular `property()` but
    // they are not synchronized.
    private final Set<String> isolatedProps = new LinkedHashSet<>();

    private RemoteWidgetState remote = null;

    // Collected in here until resumed.
    private Map<String, PropertyChange> pausedChanges = new LinkedHashMap<>();

    private final WidgetContext context;

    public WidgetPropertyContainer(WidgetContext context) {
        this.context = context;
    }

    public String getId() {
        return this.remote.getId();
    }

    // Clean up the instance from the INSTANCES map, this map holds a weak reference to the instance and should
    // therefore not prevent GC from claiming this object but we must also remove the id and reference object from the
    // map as well to avoid leaking it.
    @Override
    protected void finalize() throws Throwable {
        if (this.isOpen())
            this.close();
        super.finalize();
    }

    public boolean isOpen() {
        return this.remote != null;
    }

    public boolean isClosed() {
        return !this.isOpen();
    }

    public boolean isConnected() {
        return this.isOpen() && this.remote.isAccessible();
    }

    private void registerSyncOnUpdate() {
        // TODO this needs to be a lot smarter but for sake of faster prototyping this is it
        WidgetPropertyContainer connectedContainer = this.getRootContainer();
        this.props.keySet().stream()
                .filter(name -> !this.isolatedProps.contains(name))
                .map(this.props::get)
                .forEach(p -> p.onChange(c -> connectedContainer.sync()));
        this.inlineContainers.values().forEach(WidgetPropertyContainer::registerSyncOnUpdate);
    }

    private void connectIsolated() {
        this.inlineContainers.values().forEach(WidgetPropertyContainer::connectIsolated);
        this.isolatedProps.stream()
                .map(this.props::get)
                .forEach(sub -> ((WidgetPropertyContainer) sub.get()).connect());
    }

    public RemoteWidgetState connect() {
        // Connect any unconnected isolated sub containers **first**.
        this.connectIsolated();

        if (this.isInline())
            return this.getEnclosingContainer().connect();

        if (this.isConnected())
            return this.remote;

        context.connect(this, remote -> this.remote = remote);
        this.registerSyncOnUpdate();

        return this.remote;
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
            StatePatch patch = this.createPatch();
            this.remote.updateState(patch);
        }
    }

    protected boolean isInline() {
        return this.enclosingContainer != null;
    }

    protected WidgetPropertyContainer getEnclosingContainer() {
        return this.enclosingContainer;
    }

    protected WidgetPropertyContainer getRootContainer() {
        WidgetPropertyContainer container = this;
        while (container.getEnclosingContainer() != null)
            container = container.getEnclosingContainer();
        return container;
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

    protected <T extends WidgetPropertyContainer> T inline(String prefix, WidgetPropertyContainerConstructor<? extends T> childContainerConstructor) {
        T childContainer = childContainerConstructor.construct(this.context);
        if (childContainer.enclosingContainer != null)
            throw new IllegalStateException("Container constructor should provide a fresh instance.");

        childContainer.enclosingContainer = this;
        this.inlineContainers.put(prefix, childContainer);

        return childContainer;
    }

    protected <T extends WidgetPropertyContainer> T inline(String prefix, WidgetPropertyContainerConstructor<? extends T> childContainerConstructor, Consumer<T> configure) {
        T childContainer = this.inline(prefix, childContainerConstructor);
        configure.accept(childContainer);
        return childContainer;
    }

    protected <T extends WidgetPropertyContainer> WidgetProperty<T> isolated(String name, WidgetPropertyContainerConstructor<? extends T> constructor) {
        T child = constructor.construct(this.context);
        return this.isolated(name, child);
    }

    protected <T extends WidgetPropertyContainer> WidgetProperty<T> isolated(String name, WidgetPropertyContainerConstructor<? extends T> constructor, Consumer<T> configure) {
        T child = constructor.construct(this.context);
        WidgetProperty<T> prop = this.isolated(name, child);
        configure.accept(child);
        return prop;
    }

    private <T extends WidgetPropertyContainer> WidgetProperty<T> isolated(String name, T child) {
        WidgetProperty<T> prop = this.property(name, child);
        this.isolatedProps.add(name);
        return prop;
    }

    private <V> void onUpdate(String prop, PropertyChange<V> change) {
        // TODO fold the changes so that a change back to original is skipped
    }

    @Override
    public StatePatch createPatch(EnumSet<StatePatch.Opts> opts) {
        Gson gson = this.context.getSerializer();

        StatePatch patch = new StatePatch();
        if (opts.contains(StatePatch.Opts.INCLUDE_ALL))
            patch.putAllJson((JsonObject) gson.toJsonTree(INV_REGISTRY.get(this.getClass())));
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
                    patch.putBinary(prefix + name, ((RawDataWidgetProperty) prop).toBytes(prop.get()));
                else if (prop instanceof MultiRawDataWidgetProperty)
                    patch.putBinary(prefix + name, ((MultiRawDataWidgetProperty) prop).toBytes(prop.get()));
                else
                    patch.putJson(prefix + name, gson.toJsonTree(prop.get(), prop.getType()));

                if (clearDirty)
                    prop.setDirty(false);
            }
        });
    }

    public void applyPatch(StatePatch patch) {
        Gson gson = this.context.getSerializer();

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

            prop.setCleanly(gson.fromJson(json, prop.getType()));
        });

        patch.forEachBuffer((key, buffers) -> {
            if (!key.startsWith(prefix))
                return;
            key = key.substring(prefix.length());

            WidgetProperty prop = this.props.get(key);
            if (prop == null)
                return;

            Object value;
            if (prop instanceof RawDataWidgetProperty) {
                value = ((RawDataWidgetProperty) prop).fromBytes(buffers.get(0));
            } else if (prop instanceof MultiRawDataWidgetProperty) {
                value = ((MultiRawDataWidgetProperty) prop).fromBytes(buffers);
            } else {
                return;
            }

            prop.setCleanly(value);
        });
    }

    @Override
    public void handleCustomContent(JsonElement payload) {
        // TODO implement event listener style attachments for custom content?
    }

    public void update(Runnable updater) {
        // TODO needs to be implemented once synchronization is implemented
//        try {
//            this.pauseSync();
//            updater.run();
//        } finally {
//            this.resumeSync();
//            this.sync();
//        }
    }

    @Override
    public void render(RenderContext context) {
        context.renderIfRequested(WidgetPropertyContainer.MIME_TYPE, () -> {
            Map<String, Object> data = new LinkedHashMap<>(3);
            data.put("model_id", this.getId());
            data.put("version_major", 2);
            data.put("version_minor", 0);
            return data;
        });
    }
}

package io.github.spencerpark.jupyter.ipywidgets.props;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class WidgetPropertyContainer {
    public static <T> T configure(T container, Consumer<T> config) {
        config.accept(container);
        return container;
    }

    protected WidgetPropertyContainer parent;
    private final Map<String, WidgetProperty> props = new LinkedHashMap<>();
    private final Map<String, WidgetPropertyContainer> inlineContainers = new LinkedHashMap<>();

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

    protected <T extends WidgetPropertyContainer> T inline(String prefix, T childContainer) {
        if (childContainer.parent != null)
            throw new IllegalStateException("Container is already a child of another container.");

        childContainer.parent = this;
        this.inlineContainers.put(prefix, childContainer);

        return childContainer;
    }

    protected <T extends WidgetPropertyContainer> T inline(String prefix, T childContainer, Consumer<T> configure) {
        configure.accept(childContainer);
        return this.inline(prefix, childContainer);
    }

    protected <T extends WidgetPropertyContainer> WidgetProperty<T> child(String key, T child) {
        // TODO this should serialize/deserialize by widget id
    }

    public StatePatch createPatch() {
        Gson gson = WidgetsGson.getThreadLocalInstance();

        StatePatch patch = new StatePatch();
        this.populatePatch("", gson, patch);

        return patch;
    }

    private void populatePatch(String prefix, Gson gson, StatePatch patch) {
        this.inlineContainers.forEach((name, child) ->
                child.populatePatch(prefix + name, gson, patch));

        this.props.forEach((name, prop) -> {
            if (prop.isDirty()) {
                if (prop instanceof RawDataWidgetProperty)
                    patch.putBinary(prefix + name, ((RawDataWidgetProperty) prop).toBytes());
                else if (prop instanceof MultiRawDataWidgetProperty)
                    patch.putBinary(prefix + name, ((MultiRawDataWidgetProperty) prop).toBytes());
                else
                    patch.putJson(prefix + name, gson.toJsonTree(prop.get(), prop.getType()));
            }
        });
    }

    public void applyPatch(StatePatch patch) {
        Gson gson = WidgetsGson.getThreadLocalInstance();

        this.extractPatch("", gson, patch);
    }

    private void extractPatch(String prefix, Gson gson, StatePatch patch) {
        // Let the inlineContainers take what they need from the patch.
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
}

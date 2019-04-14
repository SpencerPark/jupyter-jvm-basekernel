package io.github.spencerpark.jupyter.ipywidgets.props;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseProperty<V> implements WidgetProperty<V> {
    private V value;
    private boolean dirty = true;
    private Consumer<PropertyChange<V>> listener = null;

    public BaseProperty(V value, Consumer<PropertyChange<V>> listener) {
        this.validate(value);
        this.value = value;
        this.listener = listener;
    }

    public BaseProperty(V value) {
        this(value, null);
    }

    protected void validate(V next) throws InvalidPropertyValueException {

    }

    @Override
    public V get() {
        return this.value;
    }

    @Override
    public WidgetProperty<V> set(V value) {
        this.validate(value);

        if (this.listener != null) {
            PropertyChange<V> change = new PropertyChange<>(this.value, value, this.dirty);
            this.listener.accept(change);
        }

        this.value = value;
        this.dirty = true;

        return this;
    }

    @Override
    public WidgetProperty<V> setCleanly(V value) {
        this.validate(value);

        this.value = value;

        return this;
    }

    @Override
    public WidgetProperty<V> compute(Function<V, V> transformer) {
        this.set(transformer.apply(this.get()));

        return this;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void onChange(Consumer<PropertyChange<V>> listener) {
        if (this.listener == null)
            this.listener = listener;
        else
            this.listener = change -> {
                this.listener.accept(change);
                listener.accept(change);
            };
    }
}

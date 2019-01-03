package io.github.spencerpark.jupyter.ipywidgets.props;

import java.util.function.Function;

public abstract class BaseProperty<V> implements WidgetProperty<V> {
    private V value;
    private boolean dirty = true;

    public BaseProperty(V value) {
        this.validate(value);
        this.value = value;
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
        this.value = value;
        this.dirty = true;

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
}

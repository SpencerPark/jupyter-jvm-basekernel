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
    public void set(V value) {
        this.validate(value);

        V oldValue = this.value;
        boolean wasDirty = this.dirty;

        this.value = value;
        this.dirty = true;

        if (this.listener != null) {
            PropertyChange<V> change = new PropertyChange<>(oldValue, value, wasDirty);
            this.listener.accept(change);
        }

    }

    @Override
    public void setCleanly(V value) {
        this.validate(value);

        this.value = value;

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
        if (this.listener == null) {
            this.listener = listener;
        } else {
            Consumer<PropertyChange<V>> oldListener = this.listener;
            this.listener = change -> {
                oldListener.accept(change);
                listener.accept(change);
            };
        }
    }
}

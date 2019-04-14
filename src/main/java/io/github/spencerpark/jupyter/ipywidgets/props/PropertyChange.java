package io.github.spencerpark.jupyter.ipywidgets.props;

public final class PropertyChange<V> {
    private final V oldValue;
    private final V newValue;
    private final boolean wasDirty;

    public PropertyChange(V oldValue, V newValue, boolean wasDirty) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.wasDirty = wasDirty;
    }

    public V getOldValue() {
        return oldValue;
    }

    public V getNewValue() {
        return newValue;
    }

    public boolean wasDirty() {
        return wasDirty;
    }
}

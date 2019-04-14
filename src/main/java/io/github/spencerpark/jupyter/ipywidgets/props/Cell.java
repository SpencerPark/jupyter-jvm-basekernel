package io.github.spencerpark.jupyter.ipywidgets.props;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * A memory container for a widget property value.
 *
 * @param <V> the type of the value.
 */
public final class Cell<V> {
    private final Type type;
    private V value;
    private boolean dirty = true;

    public Cell(Type type, V value) {
        this.type = type;
        this.value = value;
    }

    public V get() {
        return this.value;
    }

    public void set(V value) {
        this.value = value;
        this.dirty = true;
    }

    public void compute(Function<V, V> transformer) {
        this.set(transformer.apply(this.get()));
    }

    public Type getType() {
        return this.type;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}

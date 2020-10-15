package io.github.spencerpark.jupyter.ipywidgets.props;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;

public interface WidgetProperty<V> {
    public V get();

    public void set(V value);

    /**
     * Set the value of the parameter without changing the dirty status. Used mainly
     * for value synchronization. This also skips triggering the update listeners.
     *
     * @param value the new value for the property.
     */
    public void setCleanly(V value);

    public default void compute(Function<V, V> transformer) {
        this.set(transformer.apply(this.get()));
    }


    public Type getType();

    public boolean isDirty();

    public void setDirty(boolean dirty);

    public void onChange(Consumer<PropertyChange<V>> listener);
}

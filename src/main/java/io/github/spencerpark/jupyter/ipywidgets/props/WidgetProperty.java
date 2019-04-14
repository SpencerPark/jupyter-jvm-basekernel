package io.github.spencerpark.jupyter.ipywidgets.props;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;

public interface WidgetProperty<V> {
    public V get();

    public WidgetProperty<V> set(V value);

    /**
     * Set the value of the parameter without changing the dirty status. Used mainly
     * for value synchronization. This also skips triggering the update listeners.
     *
     * @param value the new value for the property.
     *
     * @return the same instance for chaining.
     */
    public WidgetProperty<V> setCleanly(V value);

    public WidgetProperty<V> compute(Function<V, V> transformer);


    public Type getType();

    public boolean isDirty();

    public void setDirty(boolean dirty);

    public void onChange(Consumer<PropertyChange<V>> listener);
}

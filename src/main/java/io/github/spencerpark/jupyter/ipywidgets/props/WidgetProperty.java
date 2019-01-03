package io.github.spencerpark.jupyter.ipywidgets.props;

import java.lang.reflect.Type;
import java.util.function.Function;

public interface WidgetProperty<V> {
    public V get();

    public WidgetProperty<V> set(V value);

    public WidgetProperty<V> compute(Function<V, V> transformer);


    public Type getType();

    public boolean isDirty();

    public void setDirty(boolean dirty);
}

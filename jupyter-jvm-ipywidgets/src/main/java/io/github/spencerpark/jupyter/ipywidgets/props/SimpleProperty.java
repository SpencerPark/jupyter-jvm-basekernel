package io.github.spencerpark.jupyter.ipywidgets.props;

import java.lang.reflect.Type;

public class SimpleProperty<T> extends BaseProperty<T> {
    private final Type type;

    public SimpleProperty(Type type, T value) {
        super(value);
        this.type = type;
    }

    @Override
    public Type getType() {
        return this.type;
    }
}

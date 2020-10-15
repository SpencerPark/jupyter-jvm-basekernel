package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

import java.util.function.Consumer;

public interface WidgetContext {
    public default <T extends WidgetPropertyContainer> T inflate(WidgetPropertyContainerConstructor<? extends T> constructor) {
        T widget = constructor.construct(this);
        widget.connect();
        return widget;
    }

    public RemoteWidgetState connect(WidgetPropertyContainer container, Consumer<RemoteWidgetState> initialize);

    public WidgetPropertyContainer lookupInstance(String id);

    public Gson getSerializer();
}

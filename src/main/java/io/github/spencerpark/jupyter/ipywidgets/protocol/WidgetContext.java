package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

import java.util.UUID;

public interface WidgetContext {
    public default <T extends WidgetPropertyContainer> T inflate(WidgetPropertyContainerConstructor<? extends T> constructor) {
        T widget = constructor.construct(this);
        widget.connect();
        return widget;
    }

    public RemoteWidgetState connect(WidgetPropertyContainer container);

    public UUID registerInstance(WidgetPropertyContainer instance);

    public void unregisterInstance(WidgetPropertyContainer instance);

    public WidgetPropertyContainer lookupInstance(UUID id);

    public Gson getSerializer();
}

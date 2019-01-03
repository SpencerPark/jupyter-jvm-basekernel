package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;

public class WidgetCoordinates extends WidgetPropertyContainer {
    public final ComponentCoordinates model = super.inline("_model_", new ComponentCoordinates(), c -> {
        c.name.set("WidgetModel");
        c.module.set("@jupyter-widgets/base");
        c.moduleVersion.set(ProtocolConstants.JUPYTER_WIDGETS_BASE_VERSION);
    });

    public final ComponentCoordinates view = super.inline("_view_", new ComponentCoordinates(), c -> {
        c.moduleVersion.set("");
    });
}

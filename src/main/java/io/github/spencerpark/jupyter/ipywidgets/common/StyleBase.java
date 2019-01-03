package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;

public class StyleBase extends WidgetPropertyContainer {
    public final WidgetCoordinates coords = super.inline("", new WidgetCoordinates(), c -> {
        c.model.name.set("StyleModel");
        c.view.name.set("StyleView");
        c.view.module.set("@jupyter-widgets/base");
        c.view.moduleVersion.set(ProtocolConstants.JUPYTER_WIDGETS_BASE_VERSION);
    });
}

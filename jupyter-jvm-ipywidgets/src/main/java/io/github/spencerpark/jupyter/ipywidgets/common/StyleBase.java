package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

public class StyleBase extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            StyleBase.class,
            WidgetCoordinates.BASE.with((model, view) -> {
                model.name("StyleModel");
                view.name("StyleView").module("@jupyter-widgets/base").version(ProtocolConstants.JUPYTER_WIDGETS_BASE_VERSION);
            })
    );

    public StyleBase(WidgetContext context) {
        super(context);
    }
}

package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import static io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants.CORE_WIDGET;

public class DescriptionStyle extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            DescriptionStyle.class,
            StyleBase.COORDS
                    .with((model, view) -> model
                            .module(ProtocolConstants.JUPYTER_CORE_WIDGETS_MODULE)
                            .name("DescriptionStyleModel")
                            .version(ProtocolConstants.JUPYTER_WIDGETS_CONTROLS_VERSION))
    );

    public DescriptionStyle(WidgetContext context) {
        super(context);
    }

    public final DOMBase dom = super.inline("", DOMBase::new);

    public final WidgetProperty<String> width = super.property("description_width", String.class);
}

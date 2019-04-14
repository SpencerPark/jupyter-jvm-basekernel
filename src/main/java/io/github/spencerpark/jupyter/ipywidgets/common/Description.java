package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.Widget;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

public class Description extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            Description::new,
            DOMBase.COORDS.with((model, view) -> model.name("DescriptionModel"))
    );

    public Description(WidgetContext context) {
        super(context);
    }

    public final WidgetProperty<String> value = super.property("description", String.class, "");

    public final WidgetProperty<String> descriptionTooltip = super.property("description_tooltip", String.class);

    public final WidgetProperty<DescriptionStyle> style = super.isolated("style", DescriptionStyle::new);
}

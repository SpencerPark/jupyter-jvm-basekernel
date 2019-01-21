package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class DescriptionStyle extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            DescriptionStyle::new,
            StyleBase.COORDS.with((model, view) -> model.module("DescriptionStyleModel"))
    );

    public final WidgetProperty<String> width = super.property("description_width", String.class);
}

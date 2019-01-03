package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;

public class DescriptionStyle extends StyleBase {
    {
        super.coords.model.name.set("DescriptionStyleModel");
    }

    public final WidgetProperty<String> descriptionWidget = super.property("description_width", String.class);
}

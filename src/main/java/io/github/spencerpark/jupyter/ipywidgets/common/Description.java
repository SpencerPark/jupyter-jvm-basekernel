package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;

public class Description extends DOMBase {
    {
        super.coords.model.name.set("DescriptionModel");
    }

    public final WidgetProperty<String> description = super.property("description", String.class, "");

    public final WidgetProperty<String> descriptionTooltip = super.property("description_tooltip", String.class);

    public final WidgetProperty<DescriptionStyle> style = super.child("style", new DescriptionStyle());
}

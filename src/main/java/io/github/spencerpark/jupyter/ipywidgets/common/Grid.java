package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class Grid extends WidgetPropertyContainer {
    public final GridAuto auto = super.inline("auto_", new GridAuto());

    public final GridTemplate template = super.inline("template_", new GridTemplate());

    public final WidgetProperty<String> gap = super.property("gap", String.class);
    public final WidgetProperty<String> row = super.property("row", String.class);
    public final WidgetProperty<String> column = super.property("column", String.class);
    public final WidgetProperty<String> area = super.property("area", String.class);
}

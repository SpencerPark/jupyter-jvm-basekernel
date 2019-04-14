package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

public class Grid extends WidgetPropertyContainer {
    public Grid(WidgetContext context) {
        super(context);
    }

    public final GridAuto auto = super.inline("auto_", GridAuto::new);

    public final GridTemplate template = super.inline("template_", GridTemplate::new);

    public final WidgetProperty<String> gap = super.property("gap", String.class);
    public final WidgetProperty<String> row = super.property("row", String.class);
    public final WidgetProperty<String> column = super.property("column", String.class);
    public final WidgetProperty<String> area = super.property("area", String.class);
}

package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class GridTemplate extends WidgetPropertyContainer {
    public final WidgetProperty<String> rows = super.property("rows", String.class);
    public final WidgetProperty<String> columns = super.property("columns", String.class);
    public final WidgetProperty<String> areas = super.property("areas", String.class);
}

package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class ComponentCoordinates extends WidgetPropertyContainer {
    public final WidgetProperty<String> name = super.property("name", String.class);
    public final WidgetProperty<String> module = super.property("module", String.class);
    public final WidgetProperty<String> moduleVersion = super.property("module_version", String.class);
}

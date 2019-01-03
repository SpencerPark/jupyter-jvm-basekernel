package io.github.spencerpark.jupyter.ipywidgets.common;

import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

import java.util.List;

public class DOMBase extends WidgetPropertyContainer {
    public final WidgetCoordinates coords = super.inline("", new WidgetCoordinates(), c -> {
        c.model.name.set("DOMWidgetModel");
    });

    public final WidgetProperty<List<String>> domClasses = super.property("_dom_classes", new TypeToken<List<String>>(){}.getType());

    public final WidgetProperty<Layout> layout = super.child("layout", new Layout());
}

package io.github.spencerpark.jupyter.ipywidgets.common;

import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import java.util.List;

public class DOMBase extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            DOMBase::new,
            WidgetCoordinates.BASE.with(b -> b.model.name("DOMWidgetModel"))
    );

    public DOMBase(WidgetContext context) {
        super(context);
    }

    // TODO special props for list/map?
    public final WidgetProperty<List<String>> classes = super.property("_dom_classes", new TypeToken<List<String>>() {}.getType());

    public final WidgetProperty<Layout> layout = super.isolated("layout", Layout::new);
}

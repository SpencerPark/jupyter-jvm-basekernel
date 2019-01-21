package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class IntText extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            IntText::new,
            Description.COORDS.with(b -> {
                b.model.name("IntTextModel");
                b.view.name("IntTextView");
            })
    );

    public final Description description = super.inline("", new Description());

    public final WidgetProperty<Integer> value = super.property("value", Integer.class, 0);

    public final WidgetProperty<Boolean> disabled = super.property("disabled", Boolean.class, false);

    public final WidgetProperty<Boolean> continuousUpdate = super.property("continuous_update", Boolean.class, false);

    public final WidgetProperty<Integer> step = super.property("step", Integer.class, 1);
}

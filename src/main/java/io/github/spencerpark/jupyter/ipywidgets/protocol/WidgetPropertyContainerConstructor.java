package io.github.spencerpark.jupyter.ipywidgets.protocol;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

@FunctionalInterface
public interface WidgetPropertyContainerConstructor<C extends WidgetPropertyContainer> {
    public C construct(WidgetContext context);
}

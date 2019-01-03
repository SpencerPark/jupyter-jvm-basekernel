package io.github.spencerpark.jupyter.ipywidgets;

public interface WidgetRegistry {
    public Class<? extends Widget> lookup(String model);
}

package io.github.spencerpark.jupyter.ipywidgets.protocol;

import io.github.spencerpark.jupyter.ipywidgets.common.ComponentCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.gson.JsonInline;

public class WidgetState {
    @JsonInline("_model_{0}")
    private ComponentCoordinates model;

    @JsonInline("_view_{0}")
    private ComponentCoordinates view;

    private WidgetState() {
        // For GSON
    }

    public WidgetState(ComponentCoordinates model, ComponentCoordinates view) {
        this.model = model;
        this.view = view;
    }

    public ComponentCoordinates getModelCoordinates() {
        return model;
    }

    public ComponentCoordinates getViewCoordinates() {
        return view;
    }
}

package io.github.spencerpark.jupyter.ipywidgets.protocol;

import io.github.spencerpark.jupyter.ipywidgets.props.ComponentCoordinates;

import java.util.function.BiConsumer;

public class ProtocolConstants {
    public static final String JUPYTER_WIDGETS_BASE_VERSION = "1.1.0";
    public static final String JUPYTER_WIDGETS_OUTPUT_VERSION = "1.0.0";
    public static final String JUPYTER_WIDGETS_CONTROLS_VERSION = "1.4.0";

    public static final String JUPYTER_CORE_WIDGETS_MODULE = "@jupyter-widgets/controls";

    public static final BiConsumer<ComponentCoordinates.Builder, ComponentCoordinates.Builder> CORE_WIDGET = (model, view) -> {
        model.module(JUPYTER_CORE_WIDGETS_MODULE).version(JUPYTER_WIDGETS_CONTROLS_VERSION);
        view.module(JUPYTER_CORE_WIDGETS_MODULE).version(JUPYTER_WIDGETS_CONTROLS_VERSION);
    };
}

package io.github.spencerpark.jupyter.ipywidgets;

import io.github.spencerpark.jupyter.kernel.comm.CommManager;

import java.util.Map;
import java.util.UUID;

public class WidgetManager {
    private CommManager commManager;
    private Map<UUID, CommManager> activeWidgets;


}

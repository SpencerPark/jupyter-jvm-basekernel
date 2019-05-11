package io.github.spencerpark.jupyter.api;

import io.github.spencerpark.jupyter.api.display.DisplayData;

public interface DisplayStream {

    public boolean isAttached();

    public void display(DisplayData data);

    public void updateDisplay(DisplayData data);

    public default void updateDisplay(String id, DisplayData data) {
        data.setDisplayId(id);
        this.updateDisplay(data);
    }
}

package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.kernel.display.DisplayData;

public interface DisplayStream {
    void display(DisplayData data);

    void updateDisplay(DisplayData data);

    void updateDisplay(String id, DisplayData data);
}

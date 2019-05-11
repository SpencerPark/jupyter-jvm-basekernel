package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.kernel.display.DisplayData;

public interface IOProvider {
    public void writeOut(String data);
    public void writeErr(String data);

    public boolean supportsStdin();
    public String readIn(String prompt, boolean isPassword);

    public void writeDisplay(DisplayData data);
    public void updateDisplay(String id, DisplayData data);

    public void clear(boolean defer);
}

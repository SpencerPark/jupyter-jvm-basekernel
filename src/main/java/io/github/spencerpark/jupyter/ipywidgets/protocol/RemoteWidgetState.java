package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonElement;

import java.io.Closeable;

public interface RemoteWidgetState extends Closeable {
    public void updateState(StatePatch patch);

    public void sendCustomContent(JsonElement content);

    public boolean isAccessible();

    @Override
    public void close();
}

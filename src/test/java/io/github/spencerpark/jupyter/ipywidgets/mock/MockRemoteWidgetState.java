package io.github.spencerpark.jupyter.ipywidgets.mock;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;

public class MockRemoteWidgetState implements RemoteWidgetState {
    @Override
    public void updateState(StatePatch patch) {

    }

    @Override
    public void sendCustomContent(JsonElement content) {

    }

    @Override
    public boolean isAccessible() {
        return false;
    }

    @Override
    public void close() {

    }
}

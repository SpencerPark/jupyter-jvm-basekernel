package io.github.spencerpark.jupyter.ipywidgets.mock;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;

import java.util.Queue;
import java.util.UUID;

public class MockRemoteWidgetState implements RemoteWidgetState {
    private boolean open = true;
    private MockWidgetState state;
    private final String id = UUID.randomUUID().toString();

    public MockRemoteWidgetState(MockWidgetState local) {
        this.state = local;
    }

    public MockWidgetState getState() {
        return this.state;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void updateState(StatePatch patch) {
        this.state.applyPatch(patch);
    }

    @Override
    public void sendCustomContent(JsonElement content) {
        this.state.handleCustomContent(content);
    }

    public Queue<JsonElement> getCustomEvents() {
        return this.state.getCustomEvents();
    }

    @Override
    public boolean isAccessible() {
        return open;
    }

    @Override
    public void close() {
        this.open = false;
    }
}

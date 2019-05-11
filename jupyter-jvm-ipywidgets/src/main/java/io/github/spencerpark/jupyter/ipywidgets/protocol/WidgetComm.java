package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.kernel.comm.Comm;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommCloseCommand;
import io.github.spencerpark.jupyter.messages.comm.CommMsgCommand;
import io.github.spencerpark.jupyter.messages.comm.CommOpenCommand;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class WidgetComm extends Comm implements RemoteWidgetState {
    public static void initializeOpenMessage(Message<CommOpenCommand> msg, StatePatch patch) {
        JsonObject state = patch.getState();
        JsonArray bufferPaths = patch.getBufferPaths();
        List<byte[]> buffers = patch.getBuffers();

        JsonObject payload = msg.getContent().getData();
        payload.add("state", state);
        if (bufferPaths != null)
            payload.add("buffer_paths", bufferPaths);
    }

    private final WidgetState state;
    private Runnable onCloseHandler;

    public WidgetComm(CommManager manager, String id, String targetName, WidgetState state) {
        super(manager, id, targetName);
        this.state = state;
    }

    @Override
    public String getId() {
        return super.getID();
    }

    @Override
    public boolean isAccessible() {
        return !super.isClosed();
    }

    @Override
    public void updateState(StatePatch patch) {
        if (patch.isEmpty()) return;

        JsonObject state = patch.getState();
        JsonArray bufferPaths = patch.getBufferPaths();
        List<byte[]> buffers = patch.getBuffers();

        JsonObject updatePayload = new JsonObject();
        updatePayload.addProperty("method", "update");
        updatePayload.add("state", state);
        if (bufferPaths != null)
            updatePayload.add("buffer_paths", bufferPaths);

        this.send(updatePayload, Collections.emptyMap(), buffers);
    }

    public void sendRequestState() {
        JsonObject requestStatePayload = new JsonObject();
        requestStatePayload.addProperty("method", "request_state");

        this.send(requestStatePayload);
    }

    @Override
    public void sendCustomContent(JsonElement data) {
        JsonObject customPayload = new JsonObject();
        customPayload.addProperty("method", "custom");
        customPayload.add("content", data);

        this.send(customPayload);
    }

    protected void receiveUpdate(JsonObject state, JsonArray bufferPaths, List<byte[]> buffers) {
        StatePatch patch = new StatePatch(state, bufferPaths, buffers);
        this.state.applyPatch(patch);
    }

    protected void receiveRequestState() {
        // Front-end is requesting the full state. Create a patch will all elements
        // and send an update message with that patch.
        StatePatch patch = this.state.createPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL, StatePatch.Opts.CLEAR_DIRTY));
        this.updateState(patch);
    }

    protected void receiveCustom(JsonElement content) {
        this.state.handleCustomContent(content);
    }

    @Override
    protected void onMessage(Message<CommMsgCommand> message) {
        CommMsgCommand content = message.getContent();
        JsonObject payload = content.getData();
        switch (payload.getAsJsonPrimitive("method").getAsString()) {
            case "update":
                JsonObject state = payload.get("state").getAsJsonObject();
                JsonElement bufferPaths = payload.get("buffer_paths");
                this.receiveUpdate(state, bufferPaths == null ? null : bufferPaths.getAsJsonArray(), message.getBlobs());
                return;
            case "request_state":
                this.receiveRequestState();
                return;
            case "custom":
                this.receiveCustom(payload.get("content"));
                return;
        }
    }

    public void onClose(Runnable onCloseHandler) {
        if (this.onCloseHandler == null) {
            this.onCloseHandler = onCloseHandler;
        } else {
            Runnable oldHandler = this.onCloseHandler;
            this.onCloseHandler = () -> {
                oldHandler.run();
                onCloseHandler.run();
            };
        }
    }

    @Override
    protected void onClose(Message<CommCloseCommand> closeMessage, boolean sending) {
        if (this.onCloseHandler != null)
            this.onCloseHandler.run();
    }
}

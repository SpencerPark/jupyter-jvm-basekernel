package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.MessageType;

public class ShutdownRequest {
    public static final MessageType<ShutdownRequest> MESSAGE_TYPE = MessageType.SHUTDOWN_REQUEST;

    public static final ShutdownRequest SHUTDOWN_AND_RESTART = new ShutdownRequest(true);
    public static final ShutdownRequest SHUTDOWN = new ShutdownRequest(false);

    protected boolean restart;

    private ShutdownRequest(boolean restart) {
        this.restart = restart;
    }

    public boolean isRestart() {
        return restart;
    }
}

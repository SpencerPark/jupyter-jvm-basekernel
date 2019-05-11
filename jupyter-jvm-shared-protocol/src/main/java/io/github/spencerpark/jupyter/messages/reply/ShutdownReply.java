package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.ShutdownRequest;

public class ShutdownReply implements ContentType<ShutdownReply>, ReplyType<ShutdownRequest> {
    public static final MessageType<ShutdownReply> MESSAGE_TYPE = MessageType.SHUTDOWN_REPLY;
    public static final MessageType<ShutdownRequest> REQUEST_MESSAGE_TYPE = MessageType.SHUTDOWN_REQUEST;

    @Override
    public MessageType<ShutdownReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<ShutdownRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    public static final ShutdownReply SHUTDOWN_AND_RESTART = new ShutdownReply(true);
    public static final ShutdownReply SHUTDOWN = new ShutdownReply(false);

    protected boolean restart;

    private ShutdownReply(boolean restart) {
        this.restart = restart;
    }

    public boolean isRestart() {
        return restart;
    }
}

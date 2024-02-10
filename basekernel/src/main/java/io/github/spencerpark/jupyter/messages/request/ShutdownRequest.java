package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.ShutdownReply;

public class ShutdownRequest implements ContentType<ShutdownRequest>, RequestType<ShutdownReply> {
    public static final MessageType<ShutdownRequest> MESSAGE_TYPE = MessageType.SHUTDOWN_REQUEST;
    public static final MessageType<ShutdownReply> REPLY_MESSAGE_TYPE = MessageType.SHUTDOWN_REPLY;

    @Override
    public MessageType<ShutdownRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<ShutdownReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }

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

package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.InterruptReply;

public class InterruptRequest implements ContentType<InterruptRequest>, RequestType<InterruptReply> {
    public static final MessageType<InterruptRequest> MESSAGE_TYPE = MessageType.INTERRUPT_REQUEST;
    public static final MessageType<InterruptReply> REPLY_MESSAGE_TYPE = MessageType.INTERRUPT_REPLY;

    @Override
    public MessageType<InterruptRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<InterruptReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }
}

package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.InterruptRequest;

public class InterruptReply implements ContentType<InterruptReply>, ReplyType<InterruptRequest> {
    public static final MessageType<InterruptReply> MESSAGE_TYPE = MessageType.INTERRUPT_REPLY;
    public static final MessageType<InterruptRequest> REQUEST_MESSAGE_TYPE = MessageType.INTERRUPT_REQUEST;

    @Override
    public MessageType<InterruptReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<InterruptRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }
}

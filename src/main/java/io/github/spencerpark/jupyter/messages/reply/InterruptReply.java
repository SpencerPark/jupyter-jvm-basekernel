package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class InterruptReply implements ContentType<InterruptReply> {
    public static final MessageType<InterruptReply> MESSAGE_TYPE = MessageType.INTERRUPT_REPLY;

    @Override
    public MessageType<InterruptReply> getType() {
        return MESSAGE_TYPE;
    }
}

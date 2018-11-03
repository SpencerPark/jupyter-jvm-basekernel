package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class InterruptRequest implements ContentType<InterruptRequest> {
    public static final MessageType<InterruptRequest> MESSAGE_TYPE = MessageType.INTERRUPT_REQUEST;

    @Override
    public MessageType<InterruptRequest> getType() {
        return MESSAGE_TYPE;
    }
}

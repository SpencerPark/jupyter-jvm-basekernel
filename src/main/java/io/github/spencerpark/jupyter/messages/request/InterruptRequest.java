package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.MessageType;

public class InterruptRequest {
    public static final MessageType<InterruptRequest> MESSAGE_TYPE = MessageType.INTERRUPT_REQUEST;
}

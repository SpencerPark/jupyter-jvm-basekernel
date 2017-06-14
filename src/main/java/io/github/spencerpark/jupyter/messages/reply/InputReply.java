package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class InputReply implements ContentType<InputReply> {
    public static final MessageType<InputReply> MESSAGE_TYPE = MessageType.INPUT_REPLY;

    @Override
    public MessageType<InputReply> getType() {
        return MESSAGE_TYPE;
    }

    protected String value;

    public InputReply(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

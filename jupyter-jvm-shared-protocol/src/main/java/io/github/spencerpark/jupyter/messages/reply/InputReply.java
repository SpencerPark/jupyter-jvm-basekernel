package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

public class InputReply implements ContentType<InputReply>, ReplyType<InputRequest> {
    public static final MessageType<InputReply> MESSAGE_TYPE = MessageType.INPUT_REPLY;
    public static final MessageType<InputRequest> REQUEST_MESSAGE_TYPE = MessageType.INPUT_REQUEST;

    @Override
    public MessageType<InputReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<InputRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    protected String value;

    public InputReply(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

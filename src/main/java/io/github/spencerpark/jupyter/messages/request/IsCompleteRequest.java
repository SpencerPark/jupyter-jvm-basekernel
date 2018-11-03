package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class IsCompleteRequest implements ContentType<IsCompleteRequest> {
    public static final MessageType<IsCompleteRequest> MESSAGE_TYPE = MessageType.IS_COMPLETE_REQUEST;

    @Override
    public MessageType<IsCompleteRequest> getType() {
        return MESSAGE_TYPE;
    }

    protected final String code;

    public IsCompleteRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

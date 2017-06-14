package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.MessageType;

public class IsCompleteRequest {
    public static final MessageType MESSAGE_TYPE = MessageType.IS_COMPLETE_REQUEST;

    protected final String code;

    public IsCompleteRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

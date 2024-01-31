package io.github.spencerpark.jupyter.messages.reply;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.adapters.JsonInline;
import io.github.spencerpark.jupyter.messages.debug.DapResponse;
import io.github.spencerpark.jupyter.messages.request.DebugRequest;

public class DebugReply implements ContentType<DebugReply>, ReplyType<DebugRequest> {
    public static final MessageType<DebugReply> MESSAGE_TYPE = MessageType.DEBUG_REPLY;
    public static final MessageType<DebugRequest> REQUEST_MESSAGE_TYPE = MessageType.DEBUG_REQUEST;

    @Override
    public MessageType<DebugReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<DebugRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    @JsonInline
    protected final DapResponse<JsonObject> dapResponse;

    public DebugReply(DapResponse<JsonObject> dapResponse) {
        this.dapResponse = dapResponse;
    }

    public DapResponse<JsonObject> getDapResponse() {
        return dapResponse;
    }
}

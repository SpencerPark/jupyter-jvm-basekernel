package io.github.spencerpark.jupyter.messages.reply;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;
import io.github.spencerpark.jupyter.messages.request.DebugRequest;

public class DebugReply implements JsonBox, ContentType<DebugReply>, ReplyType<DebugRequest> {
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

    protected final JsonElement dapResponse;

    public DebugReply(JsonElement dapResponse) {
        this.dapResponse = dapResponse;
    }

    @JsonBox.Unboxer
    public JsonElement getDapResponse() {
        return dapResponse;
    }
}

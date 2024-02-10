package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;
import io.github.spencerpark.jupyter.messages.reply.DebugReply;

// Debug request/reply is on control channel.
public class DebugRequest implements JsonBox, ContentType<DebugRequest>, RequestType<DebugReply> {
    public static final MessageType<DebugRequest> MESSAGE_TYPE = MessageType.DEBUG_REQUEST;
    public static final MessageType<DebugReply> REPLY_MESSAGE_TYPE = MessageType.DEBUG_REPLY;

    @Override
    public MessageType<DebugRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<DebugReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }

    protected final JsonBox.Wrapper dapRequest;

    public DebugRequest(JsonBox.Wrapper dapRequest) {
        this.dapRequest = dapRequest;
    }

    @JsonBox.Unboxer
    public JsonBox.Wrapper getDapRequest() {
        return dapRequest;
    }
}

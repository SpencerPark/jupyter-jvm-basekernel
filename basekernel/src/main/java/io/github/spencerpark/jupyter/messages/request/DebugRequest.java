package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.adapters.JsonInline;
import io.github.spencerpark.jupyter.messages.debug.DapRequest;
import io.github.spencerpark.jupyter.messages.reply.DebugReply;

// Debug request/reply is on control channel.
public class DebugRequest implements ContentType<DebugRequest>, RequestType<DebugReply> {
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

    @JsonInline
    protected final DapRequest<JsonObject> dapRequest;

    public DebugRequest(DapRequest<JsonObject> dapRequest) {
        this.dapRequest = dapRequest;
    }

    public DapRequest<JsonObject> getDapRequest() {
        return dapRequest;
    }
}

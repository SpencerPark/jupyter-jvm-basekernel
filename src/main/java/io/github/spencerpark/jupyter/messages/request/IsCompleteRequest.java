package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.IsCompleteReply;

public class IsCompleteRequest implements ContentType<IsCompleteRequest>, RequestType<IsCompleteReply> {
    public static final MessageType<IsCompleteRequest> MESSAGE_TYPE = MessageType.IS_COMPLETE_REQUEST;
    public static final MessageType<IsCompleteReply> REPLY_MESSAGE_TYPE = MessageType.IS_COMPLETE_REPLY;

    @Override
    public MessageType<IsCompleteRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<IsCompleteReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }

    protected final String code;

    public IsCompleteRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

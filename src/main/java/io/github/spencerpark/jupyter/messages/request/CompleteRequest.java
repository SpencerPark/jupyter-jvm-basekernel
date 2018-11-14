package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.CompleteReply;

public class CompleteRequest implements ContentType<CompleteRequest>, RequestType<CompleteReply> {
    public static final MessageType<CompleteRequest> MESSAGE_TYPE = MessageType.COMPLETE_REQUEST;
    public static final MessageType<CompleteReply> REPLY_MESSAGE_TYPE = MessageType.COMPLETE_REPLY;

    @Override
    public MessageType<CompleteRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<CompleteReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }

    protected final String code;

    @SerializedName("cursor_pos")
    protected final int cursorPos;

    public CompleteRequest(String code, int cursorPos) {
        this.code = code;
        this.cursorPos = cursorPos;
    }

    public String getCode() {
        return code;
    }

    public int getCursorPos() {
        return cursorPos;
    }
}

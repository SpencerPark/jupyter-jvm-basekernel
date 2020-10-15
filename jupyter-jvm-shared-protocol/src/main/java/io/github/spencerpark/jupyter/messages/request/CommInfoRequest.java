package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.CommInfoReply;

public class CommInfoRequest implements ContentType<CommInfoRequest>, RequestType<CommInfoReply> {
    public static final MessageType<CommInfoRequest> MESSAGE_TYPE = MessageType.COMM_INFO_REQUEST;
    public static final MessageType<CommInfoReply> REPLY_MESSAGE_TYPE = MessageType.COMM_INFO_REPLY;

    @Override
    public MessageType<CommInfoRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<CommInfoReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
    }

    /**
     * An optional target name
     */
    @SerializedName("target_name")
    protected final String targetName;

    public CommInfoRequest(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetName() {
        return targetName;
    }
}

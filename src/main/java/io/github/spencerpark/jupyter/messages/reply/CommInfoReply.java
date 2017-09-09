package io.github.spencerpark.jupyter.messages.reply;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.Map;

public class CommInfoReply implements ContentType<CommInfoReply> {
    public static final MessageType<CommInfoReply> MESSAGE_TYPE = MessageType.COMM_INFO_REPLY;

    @Override
    public MessageType<CommInfoReply> getType() {
        return MESSAGE_TYPE;
    }

    public static class CommInfo {
        @SerializedName("target_name")
        protected final String targetName;

        public CommInfo(String targetName) {
            this.targetName = targetName;
        }

        public String getTargetName() {
            return targetName;
        }
    }

    /**
     * A map of uuid to target_name for the comms
     */
    protected final Map<String, CommInfo> comms;

    public CommInfoReply(Map<String, CommInfo> comms) {
        this.comms = comms;
    }

    public Map<String, CommInfo> getComms() {
        return comms;
    }
}

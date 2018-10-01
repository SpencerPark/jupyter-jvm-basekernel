package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.MessageType;

public class CommInfoRequest {
    public static final MessageType<CommInfoRequest> MESSAGE_TYPE = MessageType.COMM_INFO_REQUEST;

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

package io.github.spencerpark.jupyter.messages.comm;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.adapters.IdentityJsonElementAdapter;

public class CommOpenCommand implements ContentType<CommOpenCommand> {
    public static final MessageType<CommOpenCommand> MESSAGE_TYPE = MessageType.COMM_OPEN_COMMAND;

    @Override
    public MessageType<CommOpenCommand> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("comm_id")
    protected final String commId;

    @SerializedName("target_name")
    protected final String targetName;

    @JsonAdapter(IdentityJsonElementAdapter.class)
    protected final JsonObject data;

    public CommOpenCommand(String commId, String targetName, JsonObject data) {
        this.commId = commId;
        this.targetName = targetName;
        this.data = data;
    }

    public String getCommID() {
        return commId;
    }

    public String getTargetName() {
        return targetName;
    }

    public JsonObject getData() {
        return data;
    }
}

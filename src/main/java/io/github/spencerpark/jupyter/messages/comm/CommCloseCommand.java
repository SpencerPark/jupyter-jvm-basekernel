package io.github.spencerpark.jupyter.messages.comm;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.adapters.IdentityJsonElementAdapter;

public class CommCloseCommand implements ContentType<CommCloseCommand> {
    public static final MessageType<CommCloseCommand> MESSAGE_TYPE = MessageType.COMM_CLOSE_COMMAND;

    @Override
    public MessageType<CommCloseCommand> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("comm_id")
    protected final String commId;

    @JsonAdapter(IdentityJsonElementAdapter.class)
    protected final JsonObject data;

    public CommCloseCommand(String commId, JsonObject data) {
        this.commId = commId;
        this.data = data;
    }

    public String getCommID() {
        return commId;
    }

    public JsonObject getData() {
        return data;
    }
}

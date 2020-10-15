package io.github.spencerpark.jupyter.comm;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommOpenCommand;

public class CommOpenMessageAdapter extends CommMessageAdapter<CommOpenCommand> implements CommMessage.Open {
    public CommOpenMessageAdapter(Message<CommOpenCommand> msg) {
        super(msg);
    }

    @Override
    public String getCommID() {
        return super.msg.getContent().getCommID();
    }

    @Override
    public String getTargetName() {
        return super.msg.getContent().getTargetName();
    }

    @Override
    public JsonElement getData() {
        return super.msg.getContent().getData();
    }
}

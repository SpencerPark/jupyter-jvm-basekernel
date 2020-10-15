package io.github.spencerpark.jupyter.comm;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommCloseCommand;

public class CommCloseMessageAdapter extends CommMessageAdapter<CommCloseCommand> implements CommMessage.Close {
    public CommCloseMessageAdapter(Message<CommCloseCommand> msg) {
        super(msg);
    }

    @Override
    public String getCommID() {
        return super.msg.getContent().getCommID();
    }

    @Override
    public JsonElement getData() {
        return super.msg.getContent().getData();
    }
}

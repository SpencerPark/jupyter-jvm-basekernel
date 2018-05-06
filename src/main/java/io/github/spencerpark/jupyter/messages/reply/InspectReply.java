package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.MessageType;

public class InspectReply extends DisplayData implements ContentType<InspectReply> {
    public static final MessageType<InspectReply> MESSAGE_TYPE = MessageType.INSPECT_REPLY;

    @Override
    public MessageType<InspectReply> getType() {
        return MESSAGE_TYPE;
    }

    protected final String status = "ok";
    protected final boolean found;

    public InspectReply(boolean found, DisplayData data) {
        super(data);
        this.found = found;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFound() {
        return found;
    }
}

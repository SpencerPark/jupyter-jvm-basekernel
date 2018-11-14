package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.InspectRequest;

public class InspectReply extends DisplayData implements ContentType<InspectReply>, ReplyType<InspectRequest> {
    public static final MessageType<InspectReply> MESSAGE_TYPE = MessageType.INSPECT_REPLY;
    public static final MessageType<InspectRequest> REQUEST_MESSAGE_TYPE = MessageType.INSPECT_REQUEST;

    @Override
    public MessageType<InspectReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<InspectRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
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

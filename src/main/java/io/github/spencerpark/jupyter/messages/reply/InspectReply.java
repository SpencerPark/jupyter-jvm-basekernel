package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MIMEBundle;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.Map;

public class InspectReply implements ContentType<InspectReply> {
    public static final MessageType<InspectReply> MESSAGE_TYPE = MessageType.INSPECT_REPLY;

    @Override
    public MessageType<InspectReply> getType() {
        return MESSAGE_TYPE;
    }

    protected final String status = "ok";
    protected final boolean found;
    protected final MIMEBundle data;
    protected final Map<String, Object> metadata;

    public InspectReply(boolean found, MIMEBundle data, Map<String, Object> metadata) {
        this.found = found;
        this.data = data;
        this.metadata = metadata;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFound() {
        return found;
    }

    public MIMEBundle getData() {
        return data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}

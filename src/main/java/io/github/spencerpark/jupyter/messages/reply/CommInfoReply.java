package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.Map;

public class CommInfoReply implements ContentType<CommInfoReply> {
    public static final MessageType<CommInfoReply> MESSAGE_TYPE = MessageType.COMM_INFO_REPLY;

    @Override
    public MessageType<CommInfoReply> getType() {
        return MESSAGE_TYPE;
    }

    /**
     * A map of uuid to target_name for the comms
     */
    protected final Map<String, String> comms;

    public CommInfoReply(Map<String, String> comms) {
        this.comms = comms;
    }

    public Map<String, String> getComms() {
        return comms;
    }
}

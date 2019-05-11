package io.github.spencerpark.jupyter.comm;

import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.messages.Message;

import java.util.List;
import java.util.Map;

public class CommMessageAdapter<T> implements CommMessage {
    protected final Message<T> msg;

    public CommMessageAdapter(Message<T> msg) {
        this.msg = msg;
    }

    @Override
    public boolean hasMetadata() {
        return msg.hasMetadata();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return msg.getMetadata();
    }

    @Override
    public Map<String, Object> getNonNullMetadata() {
        return msg.getNonNullMetadata();
    }

    @Override
    public boolean hasBlobs() {
        return msg.hasBlobs();
    }

    @Override
    public List<byte[]> getBlobs() {
        return msg.getBlobs();
    }

    @Override
    public List<byte[]> getNonNullBlobs() {
        return msg.getNonNullBlobs();
    }
}

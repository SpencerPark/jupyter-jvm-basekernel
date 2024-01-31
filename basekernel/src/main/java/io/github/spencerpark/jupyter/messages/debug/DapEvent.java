package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapEvent<B> extends DapProtocolMessage {
    @SerializedName("event")
    private final String event;

    // Optional
    @SerializedName("body")
    private final B body;

    public DapEvent(int seq, String event, B body) {
        super(seq, Type.EVENT);
        this.event = event;
        this.body = body;
    }

    public String getEvent() {
        return event;
    }

    public B getBody() {
        return body;
    }
}

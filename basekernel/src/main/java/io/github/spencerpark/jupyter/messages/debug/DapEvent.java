package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapEvent<B> extends DapProtocolMessage<DapEvent<B>> {
    @SerializedName("event")
    private final DapEventType<B> event;

    // Optional
    @SerializedName("body")
    private final B body;

    public DapEvent(int seq, DapEventType<B> event, B body) {
        super(seq, Type.EVENT);
        this.event = event;
        this.body = body;
    }

    @Override
    public DapEvent<B> withSeq(int seq) {
        return new DapEvent<>(seq, this.event, this.body);
    }

    public DapEvent(int seq, DapEventType<B> event) {
        this(seq, event, null);
    }

    public DapEventType<B> getEvent() {
        return event;
    }

    public B getBody() {
        return body;
    }
}

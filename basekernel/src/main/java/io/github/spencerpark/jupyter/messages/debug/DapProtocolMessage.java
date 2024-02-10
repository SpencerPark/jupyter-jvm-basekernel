package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

/**
 * Jupyter embeds the Debug Adapter Protocol (DAP) within their existing messaging protocol. Additionally, it is
 * extended with some Jupyter specific messages/events.
 */
public class DapProtocolMessage {
    public enum Type {
        @SerializedName("request") REQUEST,
        @SerializedName("response") RESPONSE,
        @SerializedName("event") EVENT,
    }

    @SerializedName("seq")
    protected final int seq;

    @SerializedName("type")
    protected final Type type;

    public DapProtocolMessage(int seq, Type type) {
        this.seq = seq;
        this.type = type;
    }

    public int getSeq() {
        return seq;
    }

    public Type getType() {
        return type;
    }
}

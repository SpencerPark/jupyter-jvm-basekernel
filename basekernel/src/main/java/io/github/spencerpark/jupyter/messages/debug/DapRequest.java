package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapRequest<A> extends DapProtocolMessage {
    @SerializedName("command")
    private final String command;

    // Optional
    @SerializedName("arguments")
    private final A arguments;

    public DapRequest(int seq, String command, A arguments) {
        super(seq, Type.REQUEST);
        this.command = command;
        this.arguments = arguments;
    }

    public String getCommand() {
        return command;
    }

    public A getArguments() {
        return arguments;
    }
}

package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapRequest<A, B> extends DapProtocolMessage {
    @SerializedName("command")
    private final DapCommandType<A, B> command;

    // Optional
    @SerializedName("arguments")
    private final A arguments;

    public DapRequest(int seq, DapCommandType<A, B> command, A arguments) {
        super(seq, Type.REQUEST);
        this.command = command;
        this.arguments = arguments;
    }

    public DapRequest(int seq, DapCommandType<A, B> command) {
        this(seq, command, null);
    }

    public DapCommandType<A, B> getCommand() {
        return command;
    }

    public A getArguments() {
        return arguments;
    }
}

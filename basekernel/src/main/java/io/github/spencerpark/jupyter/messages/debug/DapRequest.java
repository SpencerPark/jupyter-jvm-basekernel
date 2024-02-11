package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapRequest<A, B> extends DapProtocolMessage<DapRequest<A, B>> {
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

    @Override
    public DapRequest<A, B> withSeq(int seq) {
        return new DapRequest<>(seq, this.command, this.arguments);
    }

    public DapCommandType<A, B> getCommand() {
        return command;
    }

    public A getArguments() {
        return arguments;
    }
}

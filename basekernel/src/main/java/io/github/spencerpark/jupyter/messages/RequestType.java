package io.github.spencerpark.jupyter.messages;

public interface RequestType<Rep> {
    public MessageType<Rep> getReplyType();
}

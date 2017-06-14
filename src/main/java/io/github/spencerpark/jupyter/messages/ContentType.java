package io.github.spencerpark.jupyter.messages;

public interface ContentType<T> {
    public MessageType<T> getType();
}

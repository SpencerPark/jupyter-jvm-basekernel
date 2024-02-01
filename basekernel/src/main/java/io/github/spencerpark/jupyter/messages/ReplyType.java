package io.github.spencerpark.jupyter.messages;

public interface ReplyType<Req> {
    public MessageType<Req> getRequestType();
}

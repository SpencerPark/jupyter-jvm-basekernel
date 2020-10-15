package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.channels.ReplyEnvironment;
import io.github.spencerpark.jupyter.messages.Message;

@FunctionalInterface
public interface MessageHandler<T> {
    public void handle(Message<T> message) throws Exception;

    @FunctionalInterface
    public static interface WithEnvironment<T> {
        public void handle(ReplyEnvironment env, Message<T> message) throws Exception;
    }
}

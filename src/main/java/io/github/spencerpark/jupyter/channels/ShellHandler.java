package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.messages.Message;

@FunctionalInterface
public interface ShellHandler<T> {
    public void handle(ShellReplyEnvironment env, Message<T> message);
}

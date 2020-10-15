package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.client.api.JupyterError;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;

import java.util.Collections;
import java.util.List;

class ErrorReplyAdapter implements JupyterError {
    private final ErrorReply reply;

    public ErrorReplyAdapter(ErrorReply reply) {
        this.reply = reply;
    }

    @Override
    public String getErrorName() {
        return this.reply.getErrorName();
    }

    @Override
    public String getErrorMessage() {
        return this.reply.getErrorMessage();
    }

    @Override
    public List<String> getStacktrace() {
        return Collections.unmodifiableList(this.reply.getStacktrace());
    }
}

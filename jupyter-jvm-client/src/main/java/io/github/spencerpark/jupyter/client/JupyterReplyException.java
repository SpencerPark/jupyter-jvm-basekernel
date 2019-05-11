package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.messages.reply.ErrorReply;

public class JupyterReplyException extends JupyterRuntimeException {
    private final ErrorReply reply;

    public JupyterReplyException(ErrorReply reply) {
        this.reply = reply;
    }

    public ErrorReply getReply() {
        return reply;
    }
}

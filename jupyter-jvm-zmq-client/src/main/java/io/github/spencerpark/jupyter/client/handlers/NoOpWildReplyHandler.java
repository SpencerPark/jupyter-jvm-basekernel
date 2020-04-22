package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.client.BaseZmqJupyterClient;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.reply.InputReply;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

/**
 * A reply handler that ignores all messages. This is the default behaviours clients should use for their
 * {@link BaseZmqJupyterClient#getWildReplyHandler()} unless they are tracking old active cell output areas
 * like the notebook does. A client that is connected to a kernel with multiple connected clients will publish
 * extra messages all the time and clients that did not make to original request are not expected to handle these
 * in any meaningful way. The one exception is when a kernel produces output after the idle confirmation was published
 * in which a client may be generous and handle such message.
 */
public class NoOpWildReplyHandler implements ReplyHandler<ReplyType<?>> {
    public static final NoOpWildReplyHandler INSTANCE = new NoOpWildReplyHandler();

    public static NoOpWildReplyHandler getInstance() {
        return INSTANCE;
    }

    private NoOpWildReplyHandler() { }

    @Override
    public void handleStatusUpdate(Message<PublishStatus> status) {
        // Ignore
    }

    @Override
    public void handleStreamIO(Message<PublishStream> message) {
        // Ignore
    }

    @Override
    public void handleDisplayData(Message<PublishDisplayData> message) {
        // Ignore
    }

    @Override
    public void handleUpdateDisplayData(Message<PublishUpdateDisplayData> message) {
        // Ignore
    }

    @Override
    public void handleClearOutput(Message<PublishClearOutput> message) {
        // Ignore
    }

    @Override
    public void handleExecuteResult(Message<PublishExecuteResult> message) {
        // Ignore
    }

    @Override
    public void handleError(Message<PublishError> message) {
        // Ignore
    }

    @Override
    public InputReply provideStdin(Message<InputRequest> message) {
        // Ignore
        return null;
    }

    @Override
    public void onReply(Message<? extends ReplyType<?>> message) {
        // Drop it
    }

    @Override
    public void onErrorReply(Message<ErrorReply> reply) {
        // Drop it
    }
}

package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.reply.InputReply;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

public interface ReplyHandler<R> {
    // IOPub handlers

    public void handleStreamIO(Message<PublishStream> streamData);

    public void handleDisplayData(Message<PublishDisplayData> data);

    public void handleUpdateDisplayData(Message<PublishUpdateDisplayData> data);

    public void handleClearOutput(Message<PublishClearOutput> clearData);

    public void handleExecuteResult(Message<PublishExecuteResult> result);

    public void handleError(Message<PublishError> error);

    // Stdin handlers

    public InputReply provideStdin(Message<InputRequest> request);

    // Shell handlers

    public void onReply(Message<? extends R> reply);

    public void onErrorReply(Message<ErrorReply> reply);
}

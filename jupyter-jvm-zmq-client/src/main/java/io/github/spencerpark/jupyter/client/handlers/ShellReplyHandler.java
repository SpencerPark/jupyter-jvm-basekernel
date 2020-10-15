package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.client.api.IOProvider;
import io.github.spencerpark.jupyter.client.api.Result;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.reply.InputReply;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A shell request consists of a number of messages over two main channels. The request and reply
 * are sent over the shell (or control) channel. All extra events are published on the iopub with a parent
 * header corresponding to the initial request. These pub messages are handled here. A request is considered
 * finished after an idle status with the initial request as the parent header is published and the reply is
 * received.
 *
 * @param <R>
 */
public class ShellReplyHandler<R> implements ReplyHandler<R> {
    private final MessageType<R> expectedType;
    private final IOProvider io;

    private final AtomicReference<PublishExecuteResult> result = new AtomicReference<>();
    private final AtomicReference<PublishError> error = new AtomicReference<>();
    private final AtomicReference<R> reply = new AtomicReference<>();
    private final AtomicReference<ErrorReply> errorReply = new AtomicReference<>();

    private final CompletableFuture<Void> replyPromise = new CompletableFuture<>();
    private final CompletableFuture<Void> workCompletePromise = new CompletableFuture<>();
    private final CompletableFuture<Result<R>> resultPromise = this.replyPromise.thenCombine(this.workCompletePromise,
            (replyDone, workCompleteDone) -> {
                R reply = this.reply.get();
                if (reply != null)
                    return Result.success(reply, this.result.get());
                return Result.error(
                        new ErrorReplyAdapter(this.errorReply.get()),
                        new PublishErrorAdapter(this.error.get()));
            });

    public ShellReplyHandler(MessageType<R> expectedType, IOProvider io) {
        this.expectedType = expectedType;
        this.io = io;
    }

    public MessageType<R> getExpectedReplyType() {
        return this.expectedType;
    }

    public IOProvider io() {
        return this.io;
    }

    public CompletableFuture<Result<R>> getFutureResult() {
        return this.resultPromise;
    }

    public PublishExecuteResult getResult() {
        return this.result.get();
    }

    public PublishError getError() {
        return this.error.get();
    }

    // IOPub handlers

    @Override
    public void handleStatusUpdate(Message<PublishStatus> message) {
        PublishStatus status = message.getContent();
        switch (status.getState()) {
            case IDLE:
                this.workCompletePromise.complete(null);
                break;
        }
    }

    @Override
    public void handleStreamIO(Message<PublishStream> message) {
        PublishStream streamData = message.getContent();
        if (streamData.getStreamType() == PublishStream.StreamType.OUT)
            this.io().writeOut(streamData.getText());
        else
            this.io().writeErr(streamData.getText());
    }

    @Override
    public void handleDisplayData(Message<PublishDisplayData> message) {
        this.io().writeDisplay(message.getContent());
    }

    @Override
    public void handleUpdateDisplayData(Message<PublishUpdateDisplayData> message) {
        PublishUpdateDisplayData data = message.getContent();
        String id = data.getDisplayId();
        this.io().updateDisplay(id, data);
    }

    @Override
    public void handleClearOutput(Message<PublishClearOutput> message) {
        this.io().clear(message.getContent().shouldWait());
    }

    @Override
    public void handleExecuteResult(Message<PublishExecuteResult> message) {
        this.result.set(message.getContent());
    }

    @Override
    public void handleError(Message<PublishError> message) {
        this.error.set(message.getContent());
    }

    // Stdin handlers

    @Override
    public InputReply provideStdin(Message<InputRequest> message) {
        InputRequest request = message.getContent();
        return new InputReply(this.io().readIn(request.getPrompt(), request.isPassword()));
    }

    // Shell handlers

    @Override
    public void onReply(Message<? extends R> reply) {
        this.reply.set(reply.getContent());
        this.replyPromise.complete(null);
    }

    @Override
    public void onErrorReply(Message<ErrorReply> reply) {
        this.errorReply.set(reply.getContent());
        this.replyPromise.complete(null);
    }
}

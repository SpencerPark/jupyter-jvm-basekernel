package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.client.IOProvider;
import io.github.spencerpark.jupyter.client.Result;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.reply.InputReply;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TargetedReplyHandler<R> implements ReplyHandler<R> {
    private final MessageType<R> expectedType;
    private final IOProvider io;

    private final CompletableFuture<Result<R>> promise = new CompletableFuture<>();
    private final AtomicReference<PublishExecuteResult> result = new AtomicReference<>();
    private final AtomicReference<PublishError> error = new AtomicReference<>();

    public TargetedReplyHandler(MessageType<R> expectedType, IOProvider io) {
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
        return this.promise;
    }

    public PublishExecuteResult getResult() {
        return this.result.get();
    }

    public PublishError getError() {
        return this.error.get();
    }

    // IOPub handlers

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
        this.promise.complete(Result.success(reply.getContent(), result.get()));
    }

    @Override
    public void onErrorReply(Message<ErrorReply> reply) {
        this.promise.complete(Result.error(reply.getContent(), error.get()));
    }
}

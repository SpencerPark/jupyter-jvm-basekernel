package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.api.ExpressionValue;
import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.api.ReplacementOptions;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.history.HistoryEntry;
import io.github.spencerpark.jupyter.client.api.*;
import io.github.spencerpark.jupyter.client.api.events.BeginExecutingCodeListener;
import io.github.spencerpark.jupyter.client.api.events.BusyStateChangeListener;
import io.github.spencerpark.jupyter.client.api.events.EventSubscription;
import io.github.spencerpark.jupyter.client.channels.JupyterClientConnection;
import io.github.spencerpark.jupyter.client.handlers.NoOpWildReplyHandler;
import io.github.spencerpark.jupyter.client.handlers.ReplyHandler;
import io.github.spencerpark.jupyter.client.handlers.ShellReplyHandler;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteInput;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ZmqJupyterClient extends BaseZmqJupyterClient implements JupyterClient {
    public static ZmqJupyterClient createConnectedTo(KernelConnectionProperties connectionProps) throws InvalidKeyException, NoSuchAlgorithmException {
        return createConnectedTo(new JupyterClientConnection(connectionProps));
    }

    public static ZmqJupyterClient createConnectedTo(KernelConnectionProperties connectionProps, ReplyHandler<?> wildReplyHandler) throws InvalidKeyException, NoSuchAlgorithmException {
        return createConnectedTo(new JupyterClientConnection(connectionProps), wildReplyHandler);
    }

    public static ZmqJupyterClient createConnectedTo(JupyterClientConnection connection) {
        return connectTo(new ZmqJupyterClient(), connection);
    }

    public static ZmqJupyterClient createConnectedTo(JupyterClientConnection connection, ReplyHandler<?> wildReplyHandler) {
        return connectTo(new ZmqJupyterClient(wildReplyHandler), connection);
    }

    private static ZmqJupyterClient connectTo(ZmqJupyterClient client, JupyterClientConnection connection) {
        client.connect(connection);
        connection.connect();
        return client;
    }

    // This is likely low churn so to avoid needing to always copy the map when making an execute request,
    // we will instead enforce that the reference value is immutable.
    private final AtomicReference<Map<String, String>> userExpressions = new AtomicReference<>(Collections.emptyMap());

    private volatile KernelInfo kernelInfo;

    // The listener lists must be immutable. Changes should swap the reference.
    private final AtomicBoolean isBusy = new AtomicBoolean(false);
    private final AtomicReference<List<BusyStateChangeListener>> busyListeners = new AtomicReference<>(Collections.emptyList());

    private final AtomicReference<PublishExecuteInput> mostRecentlyExecuting = new AtomicReference<>();
    private final AtomicReference<List<BeginExecutingCodeListener>> beginExecutingListeners = new AtomicReference<>(Collections.emptyList());

    private final ReplyHandler<?> wildReplyHandler;

    public ZmqJupyterClient(ReplyHandler<?> wildReplyHandler) {
        this.wildReplyHandler = wildReplyHandler;
    }

    public ZmqJupyterClient() {
        this(NoOpWildReplyHandler.getInstance());
    }

    @Override
    protected void handleKernelStatusChange(PublishStatus status) {
        switch (status.getState()) {
            case BUSY:
            case STARTING:
                this.isBusy.set(true);
                this.busyListeners.get().forEach(c -> c.onBusyStateChange(true));
                break;
            case IDLE:
                this.isBusy.set(false);
                this.busyListeners.get().forEach(c -> c.onBusyStateChange(false));
        }
    }

    @Override
    protected void handleNotifyOfExecutingCode(PublishExecuteInput input) {
        this.mostRecentlyExecuting.set(input);
        this.beginExecutingListeners.get().forEach(c -> c.onBeginExecutingCode(input.getCode(), input.getCount()));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> ReplyHandler<T> getWildReplyHandler() {
        return (ReplyHandler<T>) this.wildReplyHandler;
    }

    @Override
    public boolean isBusy() {
        return this.isBusy.get();
    }

    @Override
    public EventSubscription onBusyStateChange(BusyStateChangeListener listener) {
        this.busyListeners.updateAndGet(listeners -> {
            List<BusyStateChangeListener> next = new ArrayList<>(listeners.size() + 1);
            next.addAll(listeners);
            next.add(listener);
            return next;
        });
        return () -> this.cancelBusyStateChangeListener(listener);
    }

    public void cancelBusyStateChangeListener(BusyStateChangeListener listener) {
        this.busyListeners.updateAndGet(observers -> observers.stream()
                .filter(o -> o != listener)
                .collect(Collectors.toList()));
    }

    @Override
    public EventSubscription onBeginExecutingCode(BeginExecutingCodeListener listener) {
        this.beginExecutingListeners.updateAndGet(listeners -> {
            List<BeginExecutingCodeListener> next = new ArrayList<>(listeners.size() + 1);
            next.addAll(listeners);
            next.add(listener);
            return next;
        });
        return () -> this.cancelBeginExecutingCodeListener(listener);
    }

    public void cancelBeginExecutingCodeListener(BeginExecutingCodeListener listener) {
        this.beginExecutingListeners.updateAndGet(listeners -> listeners.stream()
                .filter(o -> o != listener)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Result<ExecutionResult>> evalAsync(String code, IOProvider io) {
        ExecuteRequest request = new ExecuteRequest(code,
                false, // silent, will suppress output (we don't want that).
                true, // store history, required to ensure that the execution count goes up
                this.userExpressions.get(),
                io != null && io.supportsStdin(),
                false // stop on error
        );

        ShellReplyHandler<ExecuteReply> replyHandler = this.performShellRequest(request, io);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply -> {
                    int count = reply.getExecutionCount();
                    Map<String, ExpressionValue> evaluatedExpressions = reply.getEvaluatedUserExpr();
                    DisplayData value = res.getPublishedValue();

                    return new ExecutionResult(count, value, evaluatedExpressions);
                }));
    }

    @Override
    public CompletableFuture<Result<Optional<DisplayData>>> inspectAsync(String code, int at, boolean extraDetail) {
        InspectRequest request = new InspectRequest(code, at, extraDetail ? 1 : 0);

        ShellReplyHandler<InspectReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply -> {
                    if (reply.isFound())
                        return Optional.of(reply);
                    else
                        return Optional.empty();
                }));
    }

    @Override
    public CompletableFuture<Result<ReplacementOptions>> completeAsync(String code, int at) {
        CompleteRequest request = new CompleteRequest(code, at);

        ShellReplyHandler<CompleteReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply ->
                        new ReplacementOptions(reply.getMatches(), reply.getCursorStart(), reply.getCursorEnd())));
    }

    @Override
    public HistoryQuery queryHistory() {
        return new BaseZmqHistoryQuery() {
            @Override
            protected CompletableFuture<Result<List<HistoryEntry>>> performQuery(HistoryRequest query) {
                ShellReplyHandler<HistoryReply> replyHandler = ZmqJupyterClient.this.performShellRequest(query, IOProvider.NULL);
                return replyHandler.getFutureResult().thenApply(res -> res.map(HistoryReply::getHistory));
            }
        };
    }

    public static final String IS_COMPLETE_YES = "complete";
    public static final String IS_COMPLETE_BAD = "invalid";
    public static final String IS_COMPLETE_MAYBE = "unknown";

    @Override
    public CompletableFuture<Result<String>> isCompleteAsync(String code) {
        IsCompleteRequest request = new IsCompleteRequest(code);

        ShellReplyHandler<IsCompleteReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply -> {
                    switch (reply.getStatus()) {
                        case VALID_CODE:
                            return IS_COMPLETE_YES;
                        case INVALID_CODE:
                            return IS_COMPLETE_BAD;
                        case UNKNOWN:
                            return IS_COMPLETE_MAYBE;
                        case NOT_FINISHED:
                        default:
                            return reply.getIndent();
                    }
                }));
    }

    @Override
    public CompletableFuture<Result<KernelInfo>> getKernelInfoAsync() {
        KernelInfoRequest request = new KernelInfoRequest();

        ShellReplyHandler<KernelInfoReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply ->
                        new KernelInfo(
                                reply.getProtocolVersion(),
                                reply.getImplementationName(),
                                reply.getImplementationVersion(),
                                reply.getLangInfo(),
                                reply.getBanner(),
                                reply.getHelpLinks())));
    }

    @Override
    public synchronized KernelInfo getKernelInfo() {
        if (this.kernelInfo == null)
            this.kernelInfo = this.getKernelInfoAsync().join().getOrThrowPublished();

        return this.kernelInfo;
    }

    @Override
    public CompletableFuture<Result<Void>> shutdownAsync() {
        ShellReplyHandler<ShutdownReply> replyHandler = this.performControlRequest(ShutdownRequest.SHUTDOWN, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res -> res.map(reply -> null));
    }

    @Override
    public CompletableFuture<Result<Void>> interruptAsync() {
        InterruptRequest request = new InterruptRequest();

        ShellReplyHandler<InterruptReply> replyHandler = this.performControlRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res -> res.map(reply -> null));
    }
}

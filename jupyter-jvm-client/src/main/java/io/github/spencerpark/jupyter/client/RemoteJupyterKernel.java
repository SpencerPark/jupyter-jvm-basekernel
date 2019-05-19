package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.client.handlers.NoOpWildReplyHandler;
import io.github.spencerpark.jupyter.client.handlers.ReplyHandler;
import io.github.spencerpark.jupyter.client.handlers.ShellReplyHandler;
import io.github.spencerpark.jupyter.api.ExpressionValue;
import io.github.spencerpark.jupyter.api.ReplacementOptions;
import io.github.spencerpark.jupyter.comm.DefaultCommManager;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.history.HistoryEntry;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteInput;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

public class RemoteJupyterKernel extends BaseJupyterClient {
    // This is likely low churn so to avoid needing to always copy the map when making an execute request,
    // we will instead enforce that the reference value is immutable.
    private final AtomicReference<Map<String, String>> userExpressions = new AtomicReference<>(Collections.emptyMap());

    private volatile KernelInfo kernelInfo;

    // The observer lists must be immutable. Changes should swap the reference.
    private final AtomicBoolean isBusy = new AtomicBoolean(false);
    private final AtomicReference<List<Consumer<Boolean>>> isBusyObservers = new AtomicReference<>(Collections.emptyList());

    private final AtomicInteger currentExecutionCount = new AtomicInteger();
    private final AtomicReference<String> currentlyExecuting = new AtomicReference<>();
    private final AtomicReference<List<ObjIntConsumer<String>>> currentlyExecutingObservers = new AtomicReference<>(Collections.emptyList());

    private final ReplyHandler<?> wildReplyHandler;

    public RemoteJupyterKernel(DefaultCommManager commManager, ReplyHandler<?> wildReplyHandler) {
        super(commManager);
        this.wildReplyHandler = wildReplyHandler;
    }

    public RemoteJupyterKernel(DefaultCommManager commManager) {
        super(commManager);
        this.wildReplyHandler = NoOpWildReplyHandler.getInstance();
    }

    @Override
    protected void handleKernelStatusChange(PublishStatus status) {
        switch (status.getState()) {
            case BUSY:
                this.isBusy.set(true);
                this.isBusyObservers.get().forEach(c -> c.accept(true));
                break;
            case IDLE:
                this.isBusy.set(false);
                // When idle clear the currently executing code string as the kernel
                // is idle...
                synchronized (this.currentExecutionCount) {
                    this.currentlyExecuting.set(null);
                }
                this.isBusyObservers.get().forEach(c -> c.accept(false));
        }
    }

    @Override
    protected void handleNotifyOfExecutingCode(PublishExecuteInput input) {
        int count = input.getCount();
        String code = input.getCode();

        synchronized (this.currentExecutionCount) {
            this.currentExecutionCount.set(count);
            this.currentlyExecuting.set(code);
        }

        this.currentlyExecutingObservers.get().forEach(c -> c.accept(code, count));
    }

    @Override
    protected <T> ReplyHandler<T> getWildReplyHandler() {
        return (ReplyHandler<T>) this.wildReplyHandler;
    }

    public boolean isBusy() {
        return this.isBusy.get();
    }

    public void onBusyStateChange(Consumer<Boolean> observer) {
        this.isBusyObservers.updateAndGet(observers -> {
            List<Consumer<Boolean>> next = new ArrayList<>(observers.size() + 1);
            next.addAll(observers);
            next.add(observer);
            return next;
        });
    }

    public void cancelBusyStateChangeListener(Consumer<Boolean> observer) {
        this.isBusyObservers.updateAndGet(observers -> observers.stream()
                .filter(o -> !o.equals(observer))
                .collect(Collectors.toList()));
    }

    public int getCurrentExecutionCount() {
        return this.currentExecutionCount.get();
    }

    public Optional<String> getCodeBeingExecuted() {
        return Optional.ofNullable(this.currentlyExecuting.get());
    }

    public void onCodeBeingExecutedChange(ObjIntConsumer<String> observer) {
        this.currentlyExecutingObservers.updateAndGet(observers -> {
            List<ObjIntConsumer<String>> next = new ArrayList<>(observers.size() + 1);
            next.addAll(observers);
            next.add(observer);
            return next;
        });
    }

    public void cancelCodeBeingExecutedChangeListener(ObjIntConsumer<String> observer) {
        this.currentlyExecutingObservers.updateAndGet(observers -> observers.stream()
                .filter(o -> !o.equals(observer))
                .collect(Collectors.toList()));
    }

    /**
     * Completes with an {@link ExecutionResult} or exceptionally with
     * a {@link JupyterPublishedException} if the executed code produced an exception
     * and a {@link JupyterReplyException} if the request produced an exception.
     *
     * @param code
     * @param io
     *
     * @return
     */
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

    public ExecutionResult eval(String code, IOProvider io) {
        return this.evalAsync(code, io).join().getOrThrowPublished();
    }

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

    public Optional<DisplayData> inspect(String code, int at, boolean extraDetail) {
        return this.inspectAsync(code, at, extraDetail).join().getOrThrowPublished();
    }

    public CompletableFuture<Result<ReplacementOptions>> completeAsync(String code, int at) {
        CompleteRequest request = new CompleteRequest(code, at);

        ShellReplyHandler<CompleteReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res ->
                res.map(reply ->
                        new ReplacementOptions(reply.getMatches(), reply.getCursorStart(), reply.getCursorEnd())));
    }

    public ReplacementOptions complete(String code, int at) {
        return this.completeAsync(code, at).join().getOrThrowPublished();
    }

    public CompletableFuture<Result<List<HistoryEntry>>> searchHistoryForAsync(HistoryQuery query) {
        HistoryRequest request = query.getRequest();

        ShellReplyHandler<HistoryReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res -> res.map(HistoryReply::getHistory));
    }

    public List<HistoryEntry> searchHistoryFor(HistoryQuery query) {
        return this.searchHistoryForAsync(query).join().getOrThrowPublished();
    }

    public static final String IS_COMPLETE_YES = "complete";
    public static final String IS_COMPLETE_BAD = "invalid";
    public static final String IS_COMPLETE_MAYBE = "unknown";

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

    public String isComplete(String code) {
        return this.isCompleteAsync(code).join().getOrThrowPublished();
    }

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

    public synchronized KernelInfo getKernelInfo() {
        if (this.kernelInfo != null)
            this.kernelInfo = this.getKernelInfoAsync().join().getOrThrowPublished();

        return this.kernelInfo;
    }

    public CompletableFuture<Result<Void>> shutdownAsync() {
        ShellReplyHandler<ShutdownReply> replyHandler = this.performShellRequest(ShutdownRequest.SHUTDOWN, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res -> res.map(reply -> null));
    }

    public void shutdown() {
        this.shutdownAsync().join().getOrThrowPublished();
    }

    public CompletableFuture<Result<Void>> interruptAsync() {
        InterruptRequest request = new InterruptRequest();

        ShellReplyHandler<InterruptReply> replyHandler = this.performShellRequest(request, IOProvider.NULL);

        return replyHandler.getFutureResult().thenApply(res -> res.map(reply -> null));
    }

    public void interrupt() {
        this.interruptAsync().join().getOrThrowPublished();
    }
}

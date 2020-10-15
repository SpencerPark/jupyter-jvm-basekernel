package io.github.spencerpark.jupyter.client.api;

import io.github.spencerpark.jupyter.api.ReplacementOptions;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.client.api.events.BeginExecutingCodeListener;
import io.github.spencerpark.jupyter.client.api.events.BusyStateChangeListener;
import io.github.spencerpark.jupyter.client.api.events.EventSubscription;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface JupyterClient extends AutoCloseable {
    boolean isBusy();

    EventSubscription onBusyStateChange(BusyStateChangeListener listener);

    EventSubscription onBeginExecutingCode(BeginExecutingCodeListener listener);

    /**
     * Completes with an {@link ExecutionResult} or exceptionally with
     * a {@link JupyterPublishedException} if the executed code produced an exception
     * and a {@link JupyterReplyException} if the request produced an exception.
     *
     * @param code the code to evaluate.
     * @param io   handlers for side effects produced as a result of evaluating the {@code code}.
     *
     * @return a future eventually resolving to the {@link Result} of evaluating the provided {@code code}.
     */
    public CompletableFuture<Result<ExecutionResult>> evalAsync(String code, IOProvider io);

    public default ExecutionResult eval(String code, IOProvider io) {
        return this.evalAsync(code, io).join().getOrThrowPublished();
    }

    public CompletableFuture<Result<Optional<DisplayData>>> inspectAsync(String code, int at, boolean extraDetail);

    public default Optional<DisplayData> inspect(String code, int at, boolean extraDetail) {
        return this.inspectAsync(code, at, extraDetail).join().getOrThrowPublished();
    }

    public CompletableFuture<Result<ReplacementOptions>> completeAsync(String code, int at);

    public default ReplacementOptions complete(String code, int at) {
        return this.completeAsync(code, at).join().getOrThrowPublished();
    }

    public HistoryQuery queryHistory();

    public static final String IS_COMPLETE_YES = "complete";
    public static final String IS_COMPLETE_BAD = "invalid";
    public static final String IS_COMPLETE_MAYBE = "unknown";

    public CompletableFuture<Result<String>> isCompleteAsync(String code);

    public default String isComplete(String code) {
        return this.isCompleteAsync(code).join().getOrThrowPublished();
    }

    public CompletableFuture<Result<KernelInfo>> getKernelInfoAsync();

    public default KernelInfo getKernelInfo() {
        return this.getKernelInfoAsync().join().getOrThrowPublished();
    }

    public CompletableFuture<Result<Void>> shutdownAsync();

    public default void shutdown() {
        this.shutdownAsync().join().getOrThrowPublished();
    }

    public CompletableFuture<Result<Void>> interruptAsync();

    public default void interrupt() {
        this.interruptAsync().join().getOrThrowPublished();
    }
}

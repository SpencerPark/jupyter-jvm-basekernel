package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.api.history.HistoryEntry;
import io.github.spencerpark.jupyter.client.api.HistoryQuery;
import io.github.spencerpark.jupyter.client.api.Result;
import io.github.spencerpark.jupyter.messages.request.HistoryRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseZmqHistoryQuery implements HistoryQuery {
    private boolean includeOutput;
    private boolean returnRaw;

    public BaseZmqHistoryQuery() {
        this(false, true);
    }

    public BaseZmqHistoryQuery(boolean includeOutput, boolean returnRaw) {
        this.includeOutput = includeOutput;
        this.returnRaw = returnRaw;
    }

    protected abstract CompletableFuture<Result<List<HistoryEntry>>> performQuery(HistoryRequest query);

    @Override
    public HistoryQuery withOutput(boolean include) {
        this.includeOutput = include;
        return this;
    }

    @Override
    public HistoryQuery returnRaw(boolean raw) {
        this.returnRaw = raw;
        return this;
    }

    @Override
    public CompletableFuture<Result<List<HistoryEntry>>> getRangeAsync(int session, int start, int stop) {
        return this.performQuery(new HistoryRequest.Range(this.includeOutput, this.returnRaw, session, start, stop));
    }

    @Override
    public CompletableFuture<Result<List<HistoryEntry>>> getTailAsync(int maxAmount) {
        return this.performQuery(new HistoryRequest.Tail(this.includeOutput, this.returnRaw, maxAmount));
    }

    @Override
    public CompletableFuture<Result<List<HistoryEntry>>> getMatchingAsync(String pattern, int maxAmount) {
        return this.performQuery(new HistoryRequest.Search(this.includeOutput, this.returnRaw, maxAmount, pattern, false));
    }

    @Override
    public CompletableFuture<Result<List<HistoryEntry>>> getUniqueMatchingAsync(String pattern, int maxAmount) {
        return this.performQuery(new HistoryRequest.Search(this.includeOutput, this.returnRaw, maxAmount, pattern, true));
    }
}

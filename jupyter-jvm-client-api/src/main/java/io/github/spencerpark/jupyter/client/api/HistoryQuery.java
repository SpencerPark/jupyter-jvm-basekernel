package io.github.spencerpark.jupyter.client.api;

import io.github.spencerpark.jupyter.api.history.HistoryEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoryQuery {
    public default HistoryQuery withOutput() {
        return this.withOutput(true);
    }

    public default HistoryQuery withoutOutput() {
        return this.withOutput(false);
    }

    public HistoryQuery withOutput(boolean include);

    public default HistoryQuery returnRaw() {
        return this.returnRaw(true);
    }

    public default HistoryQuery returnTransformed() {
        return this.returnRaw(false);
    }

    public HistoryQuery returnRaw(boolean raw);

    /**
     * Get a range of history entries for a specific session.
     *
     * @param session a session index that counts up each time the kernel
     *                starts. If negative the number is counting back from
     *                the current session.
     * @param start   start cell (execution count number) within the session.
     * @param stop    stop cell (execution count number) with the session.
     *
     * @return a promise of the history entries in the range.
     */
    public CompletableFuture<Result<List<HistoryEntry>>> getRangeAsync(int session, int start, int stop);

    public default List<HistoryEntry> getRange(int session, int start, int stop) {
        return this.getRangeAsync(session, start, stop).join().getOrThrowPublished();
    }

    /**
     * Get the last {@code maxAmount} executions in the history.
     *
     * @param maxAmount the maximum number of entries to retrieve from the last executions
     *
     * @return a promise of the last {@code maxAmount} executions in the history.
     */
    public CompletableFuture<Result<List<HistoryEntry>>> getTailAsync(int maxAmount);

    public default List<HistoryEntry> getTail(int maxAmount) {
        return this.getTailAsync(maxAmount).join().getOrThrowPublished();
    }

    /**
     * Search the session history for an execution matching the {@code pattern}.
     *
     * @param pattern glob primary filter with '*' and '?'.
     *
     * @return a promise of the last execution matching the {@code pattern}.
     */
    public default CompletableFuture<Result<List<HistoryEntry>>> getMatchingAsync(String pattern) {
        return this.getMatchingAsync(pattern, 1);
    }

    public default List<HistoryEntry> getMatching(String pattern) {
        return this.getMatchingAsync(pattern).join().getOrThrowPublished();
    }

    /**
     * Search the session history for at most {@code maxAmount} executions matching the {@code pattern}.
     *
     * @param pattern   glob primary filter with '*' and '?'.
     * @param maxAmount the limit for the number of matches to return.
     *
     * @return a promise of the last {@code maxAmount} executions matching the {@code pattern}.
     */
    public CompletableFuture<Result<List<HistoryEntry>>> getMatchingAsync(String pattern, int maxAmount);

    public default List<HistoryEntry> getMatching(String pattern, int maxAmount) {
        return this.getMatchingAsync(pattern, maxAmount).join().getOrThrowPublished();
    }

    /**
     * Search the session history for an execution matching the {@code pattern} omitting duplicate
     * entries.
     *
     * @param pattern glob primary filter with '*' and '?'.
     *
     * @return a of the last execution matching the {@code pattern}
     *         omitting duplicate entries.
     */
    public default CompletableFuture<Result<List<HistoryEntry>>> getUniqueMatchingAsync(String pattern) {
        return this.getUniqueMatchingAsync(pattern, 1);
    }

    public default List<HistoryEntry> getUniqueMatching(String pattern) {
        return this.getUniqueMatchingAsync(pattern).join().getOrThrowPublished();
    }

    /**
     * Search the session history for at most {@code maxAmount} executions matching the {@code pattern} omitting
     * duplicate entries.
     *
     * @param pattern   glob primary filter with '*' and '?'.
     * @param maxAmount the limit for the number of matches to return.
     *
     * @return a promise the last {@code maxAmount} executions matching the {@code pattern} omitting duplicate entries.
     */
    public CompletableFuture<Result<List<HistoryEntry>>> getUniqueMatchingAsync(String pattern, int maxAmount);

    public default List<HistoryEntry> getUniqueMatching(String pattern, int maxAmount) {
        return this.getUniqueMatchingAsync(pattern, maxAmount).join().getOrThrowPublished();
    }
}

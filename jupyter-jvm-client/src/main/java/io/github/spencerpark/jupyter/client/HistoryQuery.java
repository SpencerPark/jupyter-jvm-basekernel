package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.messages.request.HistoryRequest;

public class HistoryQuery {
    public static class Builder {
        private boolean includeOutput = false;
        private boolean returnRaw = true;

        public Builder includeOutput() {
            this.includeOutput = true;
            return this;
        }

        public Builder excludeOutput() {
            this.includeOutput = false;
            return this;
        }

        public Builder returnRaw() {
            this.returnRaw = true;
            return this;
        }

        public Builder returnTransformed() {
            this.returnRaw = false;
            return this;
        }

        /**
         * Get a range of history entries for a specific session.
         *
         * @param session a session index that counts up each time the kernel
         *                starts. If negative the number is counting back from
         *                the current session.
         * @param start   start cell (execution count number) within the session.
         * @param stop    stop cell (execution count number) with the session.
         *
         * @return a {@link HistoryQuery} for asking a kernel for a range of history entries for a specific session.
         */
        public HistoryQuery getRange(int session, int start, int stop) {
            HistoryRequest request = new HistoryRequest.Range(this.includeOutput, this.returnRaw, session, start, stop);
            return new HistoryQuery(request);
        }

        /**
         * Get the last {@code maxAmount} executions in the history.
         *
         * @param maxAmount the maximum number of entries to retrieve from the last executions
         *
         * @return a {@link HistoryQuery} for asking a kernel for the last {@code maxAmount} executions in the history.
         */
        public HistoryQuery getTail(int maxAmount) {
            HistoryRequest request = new HistoryRequest.Tail(this.includeOutput, this.returnRaw, maxAmount);
            return new HistoryQuery(request);
        }

        /**
         * Search the session history for an execution matching the {@code pattern}.
         *
         * @param pattern glob primary filter with '*' and '?'.
         *
         * @return a {@link HistoryQuery} for asking a kernel for the last execution matching the {@code pattern}.
         */
        public HistoryQuery getMatching(String pattern) {
            return this.getMatching(pattern, 1);
        }

        /**
         * Search the session history for at most {@code maxAmount} executions matching the {@code pattern}.
         *
         * @param pattern   glob primary filter with '*' and '?'.
         * @param maxAmount the limit for the number of matches to return.
         *
         * @return a {@link HistoryQuery} for asking a kernel for the last {@code maxAmount} executions matching the
         *         {@code pattern}.
         */
        public HistoryQuery getMatching(String pattern, int maxAmount) {
            HistoryRequest request = new HistoryRequest.Search(this.includeOutput, this.returnRaw, maxAmount, pattern, false);
            return new HistoryQuery(request);
        }

        /**
         * Search the session history for an execution matching the {@code pattern} omitting duplicate
         * entries.
         *
         * @param pattern glob primary filter with '*' and '?'.
         *
         * @return a {@link HistoryQuery} for asking a kernel for the last execution matching the {@code pattern}
         *         omitting duplicate entries.
         */
        public HistoryQuery getUniqueMatching(String pattern) {
            return this.getUniqueMatching(pattern, 1);
        }

        /**
         * Search the session history for at most {@code maxAmount} executions matching the {@code pattern} omitting
         * duplicate entries.
         *
         * @param pattern   glob primary filter with '*' and '?'.
         * @param maxAmount the limit for the number of matches to return.
         *
         * @return a {@link HistoryQuery} for asking a kernel for the last {@code maxAmount} executions matching the
         *         {@code pattern} omitting duplicate entries.
         */
        public HistoryQuery getUniqueMatching(String pattern, int maxAmount) {
            HistoryRequest request = new HistoryRequest.Search(this.includeOutput, this.returnRaw, maxAmount, pattern, true);
            return new HistoryQuery(request);
        }
    }

    private final HistoryRequest request;

    protected HistoryQuery(HistoryRequest request) {
        this.request = request;
    }

    protected HistoryRequest getRequest() {
        return request;
    }
}

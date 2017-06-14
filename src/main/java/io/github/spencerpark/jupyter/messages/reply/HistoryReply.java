package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.List;

public class HistoryReply implements ContentType<HistoryReply> {
    public static final MessageType<HistoryReply> MESSAGE_TYPE = MessageType.HISTORY_REPLY;

    @Override
    public MessageType<HistoryReply> getType() {
        return MESSAGE_TYPE;
    }

    public static class Entry {
        protected final int session;

        protected final int cellNumber;

        protected final String input;

        /**
         * null if output was specified as false in the request
         */
        protected final String output;

        public Entry(int session, int cellNumber, String input) {
            this.session = session;
            this.cellNumber = cellNumber;
            this.input = input;
            this.output = null;
        }

        public Entry(int session, int cellNumber, String input, String output) {
            this.session = session;
            this.cellNumber = cellNumber;
            this.input = input;
            this.output = output;
        }
    }

    protected final List<Entry> history;

    public HistoryReply(List<Entry> history) {
        this.history = history;
    }

    public List<Entry> getHistory() {
        return history;
    }
}

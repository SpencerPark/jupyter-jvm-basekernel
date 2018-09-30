package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.kernel.history.HistoryEntry;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.List;

public class HistoryReply implements ContentType<HistoryReply> {
    public static final MessageType<HistoryReply> MESSAGE_TYPE = MessageType.HISTORY_REPLY;

    @Override
    public MessageType<HistoryReply> getType() {
        return MESSAGE_TYPE;
    }

    protected final List<HistoryEntry> history;

    public HistoryReply(List<HistoryEntry> history) {
        this.history = history;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }
}

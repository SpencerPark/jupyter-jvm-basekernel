package io.github.spencerpark.jupyter.messages.reply;

import io.github.spencerpark.jupyter.kernel.history.HistoryEntry;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.HistoryRequest;

import java.util.List;

public class HistoryReply implements ContentType<HistoryReply>, ReplyType<HistoryRequest> {
    public static final MessageType<HistoryReply> MESSAGE_TYPE = MessageType.HISTORY_REPLY;
    public static final MessageType<HistoryRequest> REQUEST_MESSAGE_TYPE = MessageType.HISTORY_REQUEST;

    @Override
    public MessageType<HistoryReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<HistoryRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    protected final List<HistoryEntry> history;

    public HistoryReply(List<HistoryEntry> history) {
        this.history = history;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }
}

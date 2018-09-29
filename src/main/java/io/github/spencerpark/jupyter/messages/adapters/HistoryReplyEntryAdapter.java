package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.spencerpark.jupyter.messages.reply.HistoryReply;

import java.lang.reflect.Type;

public class HistoryReplyEntryAdapter implements JsonSerializer<HistoryReply.Entry> {
    public static final HistoryReplyEntryAdapter INSTANCE = new HistoryReplyEntryAdapter();

    private HistoryReplyEntryAdapter() { }

    @Override
    public JsonElement serialize(HistoryReply.Entry src, Type type, JsonSerializationContext ctx) {
        JsonArray tuple = new JsonArray();

        tuple.add(src.getSession());
        tuple.add(src.getCellNumber());

        if (src.hasOutput()) {
            JsonArray ioPair = new JsonArray();

            ioPair.add(src.getInput());
            ioPair.add(src.getOutput());

            tuple.add(ioPair);
        } else {
            tuple.add(src.getInput());
        }

        return tuple;
    }
}

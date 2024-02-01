package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.*;
import io.github.spencerpark.jupyter.messages.KernelTimestamp;

import java.lang.reflect.Type;

public class KernelTimestampAdapter implements JsonSerializer<KernelTimestamp>, JsonDeserializer<KernelTimestamp> {
    public static final KernelTimestampAdapter INSTANCE = new KernelTimestampAdapter();

    private KernelTimestampAdapter() { }

    @Override
    public KernelTimestamp deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) {
        return new KernelTimestamp(element.getAsString());
    }

    @Override
    public JsonElement serialize(KernelTimestamp timestamp, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(timestamp.getDateString());
    }
}

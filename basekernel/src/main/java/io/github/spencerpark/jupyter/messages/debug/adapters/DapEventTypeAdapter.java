package io.github.spencerpark.jupyter.messages.debug.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.spencerpark.jupyter.messages.debug.DapEventType;

import java.lang.reflect.Type;

public class DapEventTypeAdapter implements JsonSerializer<DapEventType<?>>, JsonDeserializer<DapEventType<?>> {
    public static final DapEventTypeAdapter INSTANCE = new DapEventTypeAdapter(false);
    public static final DapEventTypeAdapter UNTYPED_INSTANCE = new DapEventTypeAdapter(true);

    private final boolean untyped;

    private DapEventTypeAdapter(boolean untyped) {
        this.untyped = untyped;
    }

    @Override
    public DapEventType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return this.untyped ? DapEventType.untyped(jsonElement.getAsString()) : DapEventType.get(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(DapEventType<?> eventType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(eventType.getName());
    }
}

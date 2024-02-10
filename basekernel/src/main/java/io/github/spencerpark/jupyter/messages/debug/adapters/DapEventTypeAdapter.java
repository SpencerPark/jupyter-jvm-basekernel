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
    public static final DapEventTypeAdapter INSTANCE = new DapEventTypeAdapter();

    private DapEventTypeAdapter() {
    }

    @Override
    public DapEventType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return DapEventType.getType(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(DapEventType<?> eventType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(eventType.getName());
    }
}

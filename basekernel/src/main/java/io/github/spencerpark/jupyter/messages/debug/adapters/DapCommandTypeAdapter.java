package io.github.spencerpark.jupyter.messages.debug.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.spencerpark.jupyter.messages.debug.DapCommandType;

import java.lang.reflect.Type;

public class DapCommandTypeAdapter implements JsonSerializer<DapCommandType<?, ?>>, JsonDeserializer<DapCommandType<?, ?>> {
    public static final DapCommandTypeAdapter INSTANCE = new DapCommandTypeAdapter(false);
    public static final DapCommandTypeAdapter UNTYPED_INSTANCE = new DapCommandTypeAdapter(true);

    private final boolean untyped;

    private DapCommandTypeAdapter(boolean untyped) {
        this.untyped = untyped;
    }

    @Override
    public DapCommandType<?, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return this.untyped ? DapCommandType.untyped(jsonElement.getAsString()) : DapCommandType.get(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(DapCommandType<?, ?> commandType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(commandType.getName());
    }
}

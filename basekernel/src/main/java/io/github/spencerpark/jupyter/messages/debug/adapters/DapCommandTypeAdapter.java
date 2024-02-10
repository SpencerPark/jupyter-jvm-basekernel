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
    public static final DapCommandTypeAdapter INSTANCE = new DapCommandTypeAdapter();

    private DapCommandTypeAdapter() {
    }

    @Override
    public DapCommandType<?, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return DapCommandType.getType(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(DapCommandType<?, ?> commandType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(commandType.getName());
    }
}

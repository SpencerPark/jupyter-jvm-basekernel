package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.*;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.lang.reflect.Type;

public class MessageTypeAdapter implements JsonSerializer<MessageType<?>>, JsonDeserializer<MessageType<?>> {
    public static final MessageTypeAdapter INSTANCE = new MessageTypeAdapter();

    private MessageTypeAdapter() { }

    @Override
    public MessageType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return MessageType.getType(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(MessageType<?> messageType, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(messageType.getName());
    }
}

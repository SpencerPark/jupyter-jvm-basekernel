package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.*;
import io.github.spencerpark.jupyter.messages.Header;
import io.github.spencerpark.jupyter.messages.KernelTimestamp;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.lang.reflect.Type;

public class HeaderAdapter implements JsonSerializer<Header>, JsonDeserializer<Header> {
    public static final HeaderAdapter INSTANCE = new HeaderAdapter();

    private HeaderAdapter() { }

    @Override
    public Header deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        return new Header<>(
                object.get("msg_id").getAsString(),
                object.get("username").getAsString(),
                object.get("session").getAsString(),
                ctx.deserialize(object.get("date"), KernelTimestamp.class),
                ctx.deserialize(object.get("msg_type"), MessageType.class),
                object.get("version").getAsString()
        );
    }

    @Override
    public JsonElement serialize(Header header, Type type, JsonSerializationContext ctx) {
        JsonObject object = new JsonObject();

        object.addProperty("msg_id", header.getId());
        object.addProperty("username", header.getUsername());
        object.addProperty("session", header.getSessionId());
        object.add("date", ctx.serialize(header.getTimestamp()));
        object.add("msg_type", ctx.serialize(header.getType()));
        object.addProperty("version", header.getVersion());

        return object;
    }
}

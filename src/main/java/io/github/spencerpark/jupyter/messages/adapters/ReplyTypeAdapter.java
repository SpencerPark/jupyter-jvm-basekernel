package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;

import java.lang.reflect.Type;

public class ReplyTypeAdapter implements JsonDeserializer<ReplyType<?>> {
    public static final ReplyTypeAdapter INSTANCE = new ReplyTypeAdapter();

    private ReplyTypeAdapter() { }

    @Override
    public ReplyType<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        // If the reply is an error, decode as an ErrorReply instead of the content type
        if (jsonElement.isJsonObject()) {
            JsonElement status = jsonElement.getAsJsonObject().get("status");
            if (status != null && status.isJsonPrimitive()
                    && status.getAsString().equalsIgnoreCase("error"))
                return ctx.deserialize(jsonElement, ErrorReply.class);
        }

        return ctx.deserialize(jsonElement, type);
    }
}

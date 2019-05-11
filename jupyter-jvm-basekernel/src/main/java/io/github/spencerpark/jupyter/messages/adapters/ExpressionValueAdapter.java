package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.*;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.kernel.ExpressionValue;

import java.lang.reflect.Type;

/**
 * Decode/encode an {@link ExpressionValue} as either a {@link ExpressionValue.Error} or {@link ExpressionValue.Success}
 * based on the {@code "status"} field.
 */
public class ExpressionValueAdapter implements JsonSerializer<ExpressionValue>, JsonDeserializer<ExpressionValue> {
    public static final ExpressionValueAdapter INSTANCE = new ExpressionValueAdapter();

    private ExpressionValueAdapter() { }

    @Override
    public ExpressionValue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        if (jsonElement.isJsonObject()) {
            JsonElement status = jsonElement.getAsJsonObject().get("status");
            if (status != null && status.isJsonPrimitive()
                    && status.getAsString().equalsIgnoreCase("error"))
                return ctx.deserialize(jsonElement, ExpressionValue.Error.class);
        }

        DisplayData data = ctx.deserialize(jsonElement, DisplayData.class);
        return new ExpressionValue.Success(data);
    }

    @Override
    public JsonElement serialize(ExpressionValue exprVal, Type type, JsonSerializationContext ctx) {
        if (exprVal.isSuccess())
            return ctx.serialize(exprVal, ExpressionValue.Success.class);
        else
            return ctx.serialize(exprVal, ExpressionValue.Error.class);
    }
}

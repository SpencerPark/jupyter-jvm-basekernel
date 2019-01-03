package io.github.spencerpark.jupyter.ipywidgets;

import com.google.gson.*;

import java.lang.reflect.Type;

public class WidgetSerializer implements JsonSerializer<Widget>, JsonDeserializer<Widget> {
    public static final String SERIALIZED_MODEL_ID_PREFIX = "IPY_MODEL_";

    @Override
    public Widget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                String value = jsonPrimitive.getAsString();
                if (value.startsWith(SERIALIZED_MODEL_ID_PREFIX)) {
                    // Return widget instance.
                    String modelId = value.substring(SERIALIZED_MODEL_ID_PREFIX.length());

                }
            }
        }
        return null;
    }

    @Override
    public JsonElement serialize(Widget src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }
}

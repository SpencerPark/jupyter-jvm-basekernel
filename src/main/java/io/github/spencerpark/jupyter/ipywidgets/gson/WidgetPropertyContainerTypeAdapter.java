package io.github.spencerpark.jupyter.ipywidgets.gson;

import com.google.gson.*;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

import java.lang.reflect.Type;
import java.util.UUID;

public class WidgetPropertyContainerTypeAdapter implements JsonSerializer<WidgetPropertyContainer>, JsonDeserializer<WidgetPropertyContainer> {
    private static final String IPY_MODEL_ID_PREFIX = "IPY_MODEL_";

    public static final WidgetPropertyContainerTypeAdapter INSTANCE = new WidgetPropertyContainerTypeAdapter();

    private WidgetPropertyContainerTypeAdapter() { }

    @Override
    public JsonElement serialize(WidgetPropertyContainer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(IPY_MODEL_ID_PREFIX + src.getId().toString());
    }

    @Override
    public WidgetPropertyContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        UUID id = UUID.fromString(json.getAsString().substring(IPY_MODEL_ID_PREFIX.length()));
        return WidgetPropertyContainer.lookupInstance(id);
    }
}

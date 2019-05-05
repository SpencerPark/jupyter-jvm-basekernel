package io.github.spencerpark.jupyter.ipywidgets.gson;

import com.google.gson.*;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import java.lang.reflect.Type;

public class WidgetPropertyContainerTypeAdapter implements JsonSerializer<WidgetPropertyContainer>, JsonDeserializer<WidgetPropertyContainer> {
    private static final String IPY_MODEL_ID_PREFIX = "IPY_MODEL_";

    private final WidgetContext context;

    public WidgetPropertyContainerTypeAdapter(WidgetContext context) {
        this.context = context;
    }

    @Override
    public JsonElement serialize(WidgetPropertyContainer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(IPY_MODEL_ID_PREFIX + src.getId());
    }

    @Override
    public WidgetPropertyContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String id = json.getAsString().substring(IPY_MODEL_ID_PREFIX.length());
        return this.context.lookupInstance(id);
    }
}

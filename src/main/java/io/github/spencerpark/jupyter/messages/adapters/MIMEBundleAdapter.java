package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.messages.MIMEBundle;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class MIMEBundleAdapter implements JsonSerializer<MIMEBundle>, JsonDeserializer<MIMEBundle> {
    public static final MIMEBundleAdapter INSTANCE = new MIMEBundleAdapter();

    private final Type DATA_MAP = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();
    private MIMEBundleAdapter() { }

    @Override
    public MIMEBundle deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        Map<String, Object> data = ctx.deserialize(element, DATA_MAP);
        return new MIMEBundle(data);
    }

    @Override
    public JsonElement serialize(MIMEBundle bundle, Type type, JsonSerializationContext ctx) {
        return ctx.serialize(bundle.getData());
    }
}

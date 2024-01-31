package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Serialize all fields that are marked {@link JsonInline @JsonInline} directly in the object. It is the responsibility
 * of the defining class to ensure there is no overlap in the field names.
 */
public class JsonInlineAdapter<T> extends TypeAdapter<T> {
    private static final class InlineField {
        final String serializedName;
        final MessageFormat format;

        public InlineField(String serializedName, String prefix, String format) {
            this.serializedName = serializedName;
            this.format = new MessageFormat(
                    prefix != null && !prefix.isEmpty()
                            ? "'" + prefix + "'" + format
                            : format
            );
        }

        public String computeInlinedName(String nestedFieldName) {
            return this.format.format(new Object[]{nestedFieldName});
        }

        public String extractInlinedName(String serializedNestedFieldName) {
            try {
                return (String) this.format.parse(serializedNestedFieldName)[0];
            } catch (Exception e) {
                throw new JsonSyntaxException("Inline fields are expected to have a name with the format '" + this.format.toPattern() + "' but was " + serializedNestedFieldName);
            }
        }
    }

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        public <U> TypeAdapter<U> create(Gson gson, TypeToken<U> type) {
            TypeAdapter<U> defaultAdapter = gson.getDelegateAdapter(this, type);
            TypeAdapter<JsonObject> objectAdapter = gson.getAdapter(JsonObject.class);

            FieldNamingStrategy fieldNamingStrategy = gson.fieldNamingStrategy();

            InlineField[] inlineFields = Arrays.stream(type.getRawType().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(JsonInline.class))
                    .map(f -> {
                        SerializedName serializedNameAnnotation = f.getAnnotation(SerializedName.class);
                        String serializedName = serializedNameAnnotation == null
                                ? fieldNamingStrategy.translateName(f)
                                : serializedNameAnnotation.value();

                        JsonInline jsonInlineAnnotation = f.getAnnotation(JsonInline.class);

                        return new InlineField(serializedName, jsonInlineAnnotation.prefix(), jsonInlineAnnotation.value());
                    })
                    .toArray(InlineField[]::new);

            if (inlineFields.length > 0)
                return new JsonInlineAdapter<>(defaultAdapter, objectAdapter, inlineFields);

            return null;
        }
    };

    private final TypeAdapter<T> baseAdapter;
    private final TypeAdapter<JsonObject> objectAdapter;
    private final InlineField[] inlineFields;

    private JsonInlineAdapter(TypeAdapter<T> baseAdapter, TypeAdapter<JsonObject> objectAdapter, InlineField[] inlineFields) {
        this.baseAdapter = baseAdapter;
        this.objectAdapter = objectAdapter;
        this.inlineFields = inlineFields;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        JsonObject base = this.baseAdapter.toJsonTree(value).getAsJsonObject();

        for (InlineField field : this.inlineFields) {
            // Extract the nested value and flatten it
            JsonElement element = base.remove(field.serializedName);

            if (element.isJsonObject())
                element.getAsJsonObject().entrySet().forEach(e ->
                        base.add(field.computeInlinedName(e.getKey()), e.getValue()));
        }

        this.objectAdapter.write(out, base);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        JsonObject value = this.objectAdapter.read(in);

        JsonObject synthetic = new JsonObject();

        // Store a copy of the flattened object in a field with the serialized name
        // and let gson deserialize it from there.
        for (InlineField field : this.inlineFields) {
            JsonObject syntheticRemapped = new JsonObject();
            value.entrySet().forEach(e -> {
                String key = e.getKey();
                try {
                    syntheticRemapped.add(field.extractInlinedName(key), e.getValue());
                } catch (JsonSyntaxException ignored) {}
            });
            synthetic.add(field.serializedName, syntheticRemapped);
        }

        value.entrySet().forEach(e -> {
            if (synthetic.has(e.getKey()))
                return;

            synthetic.add(e.getKey(), e.getValue());
        });

        return this.baseAdapter.fromJsonTree(synthetic);
    }
}

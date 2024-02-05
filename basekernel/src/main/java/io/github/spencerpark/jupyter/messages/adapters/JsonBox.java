package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Marker interface for objects that box an untyped json value. They must have a constructor accepting a
 * {@link JsonBox.Wrapper} and a public method annotated with {@link JsonBox.Unboxer} that takes no arguments and
 * returns a {@link JsonBox.Wrapper}.
 * <p>
 * {@link JsonBox} objects are serialized as their unboxed value directly.
 */
public interface JsonBox {
    static GsonBuilder registerTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<?> rawType = typeToken.getRawType();
                if (!JsonBox.class.isAssignableFrom(rawType)) {
                    return null;
                }

                try {
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    MethodHandle constructor = lookup.findConstructor(rawType, MethodType.methodType(void.class, Wrapper.class));

                    MethodHandle unboxer = null;
                    for (Method method : rawType.getMethods()) {
                        // e.g. @Unboxer Wrapper get() { ... }
                        if (method.isAnnotationPresent(Unboxer.class)
                            && method.getParameterCount() == 0
                            && Wrapper.class.isAssignableFrom(method.getReturnType())
                        ) {
                            unboxer = lookup.unreflect(method);
                            break;
                        }
                    }
                    if (unboxer == null) {
                        return null;
                    }

                    return new JsonBoxAdapter<>(gson, constructor, unboxer);
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    return null;
                }
            }
        });

        builder.registerTypeAdapter(JsonStringWrapper.class,
                (JsonSerializer<JsonStringWrapper>) (src, typeOfSrc, context) -> JsonParser.parseString(src.json));
        builder.registerTypeAdapter(JsonElementWrapper.class,
                (JsonSerializer<JsonElementWrapper>) (src, typeOfSrc, context) -> src.element);
        builder.registerTypeAdapter(GsonSerializableWrapper.class,
                (JsonSerializer<GsonSerializableWrapper>) (src, typeOfSrc, context) -> context.serialize(src.value, typeOfSrc));

        builder.registerTypeAdapter(Wrapper.class,
                (JsonDeserializer<Wrapper>) (json, typeOfT, context) -> Wrapper.of(json));

        return builder;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Unboxer {}

    interface Wrapper {
        static Wrapper of(String json) {
            return new JsonStringWrapper(json);
        }

        static Wrapper of(JsonElement element) {
            return new JsonElementWrapper(element);
        }

        static Wrapper ofSerializable(Object serializable) {
            return new GsonSerializableWrapper(serializable);
        }

        <V> V unwrap(Gson gson, Class<? extends V> type);
    }
}

final class JsonStringWrapper implements JsonBox.Wrapper {
    final String json;

    JsonStringWrapper(String json) {
        this.json = json;
    }

    @Override
    public <V> V unwrap(Gson gson, Class<? extends V> type) {
        return gson.fromJson(this.json, type);
    }
}

final class JsonElementWrapper implements JsonBox.Wrapper {
    final JsonElement element;

    JsonElementWrapper(JsonElement element) {
        this.element = element;
    }

    @Override
    public <V> V unwrap(Gson gson, Class<? extends V> type) {
        return gson.fromJson(this.element, type);
    }
}

final class GsonSerializableWrapper implements JsonBox.Wrapper {
    final Object value;

    GsonSerializableWrapper(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V unwrap(Gson gson, Class<? extends V> type) {
        if (type.isInstance(this.value)) {
            return (V) this.value;
        }
        throw new JsonSyntaxException("Value of type " + this.value.getClass() + " is not assignable to " + type);
    }
}

final class JsonBoxAdapter<V> extends TypeAdapter<V> {

    private final Gson gson;
    private final MethodHandle constructor;
    private final MethodHandle unboxer;

    JsonBoxAdapter(Gson gson, MethodHandle constructor, MethodHandle unboxer) {
        this.gson = gson;
        this.constructor = constructor;
        this.unboxer = unboxer;
    }

    @Override
    public void write(JsonWriter out, V value) throws IOException {
        try {
            JsonBox.Wrapper unboxed = (JsonBox.Wrapper) this.unboxer.invoke(value);
            this.gson.toJson(unboxed, JsonBox.Wrapper.class, out);
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V read(JsonReader in) throws IOException {
        JsonElement content = JsonParser.parseReader(in);
        try {
            return (V) this.constructor.invokeExact(JsonBox.Wrapper.of(content));
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
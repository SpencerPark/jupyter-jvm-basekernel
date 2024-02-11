package io.github.spencerpark.jupyter.messages.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * {@link JsonElement} and a public method annotated with {@link JsonBox.Unboxer} that takes no arguments and returns a
 * {@link JsonElement}.
 * <p>
 * {@link JsonBox} objects are serialized as their unboxed value directly.
 */
public interface JsonBox {
    static GsonBuilder registerTypeAdapters(GsonBuilder builder) {
        final Logger LOG = LoggerFactory.getLogger(JsonBox.class);

        builder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<?> rawType = typeToken.getRawType();
                if (!JsonBox.class.isAssignableFrom(rawType)) {
                    return null;
                }

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle constructor;
                try {
                    constructor = lookup.findConstructor(rawType, MethodType.methodType(void.class, JsonElement.class));
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    LOG.warn("JsonBox type {} must have an accessible constructor with a single {} parameter. Disabling (de)serialization for it.", rawType, JsonElement.class);
                    return null;
                }

                MethodHandle unboxer = null;
                for (Method method : rawType.getMethods()) {
                    // e.g. @Unboxer JsonElement get() { ... }
                    if (method.isAnnotationPresent(Unboxer.class)) {
                        if (method.getParameterCount() == 0
                            && JsonElement.class.isAssignableFrom(method.getReturnType())) {
                            try {
                                unboxer = lookup.unreflect(method);
                            } catch (IllegalAccessException e) {
                                LOG.warn("JsonBox type {} @Unboxer method is inaccessible. Disabling (de)serialization for it.", rawType);
                                return null;
                            }
                            break;
                        } else {
                            LOG.warn("JsonBox type {} has invalid @Unboxer method. It must have no parameters and return a {}. Disabling (de)serialization for it.", rawType, JsonElement.class);
                            return null;
                        }
                    }
                }
                if (unboxer == null) {
                    LOG.warn("JsonBox type {} is missing an @Unboxer method. Disabling (de)serialization for it.", rawType);
                    return null;
                }

                return new JsonBoxAdapter<>(gson, constructor, unboxer);
            }
        });

        return builder;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Unboxer {}
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
            JsonElement unboxed = (JsonElement) this.unboxer.invoke(value);
            if (unboxed == null) {
                out.nullValue();
            } else {
                this.gson.toJson(unboxed, out);
            }
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V read(JsonReader in) throws IOException {
        JsonElement content = JsonParser.parseReader(in);
        try {
            return (V) this.constructor.invoke(content);
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
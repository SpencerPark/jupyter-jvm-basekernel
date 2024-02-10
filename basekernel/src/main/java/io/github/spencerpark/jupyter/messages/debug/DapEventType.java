package io.github.spencerpark.jupyter.messages.debug;

import io.github.spencerpark.jupyter.messages.adapters.JsonBox;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DapEventType<B> {
    private static final Map<String, DapEventType<?>> TYPE_BY_NAME = new ConcurrentHashMap<>();

    public static Optional<DapEventType<?>> lookup(String name) {
        return Optional.ofNullable(TYPE_BY_NAME.get(name));
    }

    public static DapEventType<?> getType(String name) {
        DapEventType<?> type = TYPE_BY_NAME.get(name);
        return type == null ? unknown(name) : type;
    }

    private static <B> DapEventType<B> registerType(String name, Class<B> bodyType) {
        DapEventType<B> type = new DapEventType<>(name, bodyType);
        if (TYPE_BY_NAME.size() < 1024) {
            TYPE_BY_NAME.put(name, type);
        }
        return type;
    }

    private synchronized static DapEventType<?> unknown(String name) {
        DapEventType<?> type = TYPE_BY_NAME.get(name);
        if (type != null) {
            return type;
        }

        return registerType(name, JsonBox.Wrapper.class);
    }

    private final String name;
    private final Class<B> bodyType;

    private DapEventType(String name, Class<B> bodyType) {
        this.name = name;
        this.bodyType = bodyType;
        if (name != null) {
            TYPE_BY_NAME.put(name, this);
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<B> getBodyType() {
        return this.bodyType;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DapEventType<?> that = (DapEventType<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

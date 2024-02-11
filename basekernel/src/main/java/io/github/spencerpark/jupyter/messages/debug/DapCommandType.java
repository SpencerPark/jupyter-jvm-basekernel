package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.messages.debug.arguments.CopyToGlobalsArguments;
import io.github.spencerpark.jupyter.messages.debug.arguments.DumpCellArguments;
import io.github.spencerpark.jupyter.messages.debug.arguments.RichInspectVariablesArguments;
import io.github.spencerpark.jupyter.messages.debug.bodies.CopyToGlobalsBody;
import io.github.spencerpark.jupyter.messages.debug.bodies.DebugInfoBody;
import io.github.spencerpark.jupyter.messages.debug.bodies.DumpCellBody;
import io.github.spencerpark.jupyter.messages.debug.bodies.InspectVariablesBody;
import io.github.spencerpark.jupyter.messages.debug.bodies.RichInspectVariablesBody;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DapCommandType<A, B> {
    private static final Map<String, DapCommandType<?, ?>> TYPE_BY_NAME = new ConcurrentHashMap<>();

    public static Optional<DapCommandType<?, ?>> lookup(String name) {
        return Optional.ofNullable(TYPE_BY_NAME.get(name));
    }

    public static DapCommandType<?, ?> get(String name) {
        DapCommandType<?, ?> type = TYPE_BY_NAME.get(name);
        return type == null ? unknown(name) : type;
    }

    private static <A, B> DapCommandType<A, B> register(String name, Class<A> argumentsType, Class<B> bodyType) {
        DapCommandType<A, B> type = new DapCommandType<>(name, argumentsType, bodyType);
        if (TYPE_BY_NAME.size() < 1024) {
            TYPE_BY_NAME.put(name, type);
        }
        return type;
    }

    private synchronized static DapCommandType<?, ?> unknown(String name) {
        DapCommandType<?, ?> type = TYPE_BY_NAME.get(name);
        if (type != null) {
            return type;
        }

        return register(name, JsonElement.class, JsonElement.class);
    }

    public static DapCommandType<JsonElement, JsonElement> untyped(String name) {
        return new DapCommandType<>(name, JsonElement.class, JsonElement.class);
    }

    public static final DapCommandType<DumpCellArguments, DumpCellBody> DUMP_CELL = register("dumpCell", DumpCellArguments.class, DumpCellBody.class);
    public static final DapCommandType<Void, DebugInfoBody> DEBUG_INFO = register("debugInfo", void.class, DebugInfoBody.class);
    public static final DapCommandType<Void, InspectVariablesBody> INSPECT_VARIABLES = register("inspectVariables", void.class, InspectVariablesBody.class);
    public static final DapCommandType<RichInspectVariablesArguments, RichInspectVariablesBody> RIGHT_INSPECT_VARIABLES = register("richInspectVariables", RichInspectVariablesArguments.class, RichInspectVariablesBody.class);
    public static final DapCommandType<CopyToGlobalsArguments, CopyToGlobalsBody> COPY_TO_GLOBALS = register("copyToGlobals", CopyToGlobalsArguments.class, CopyToGlobalsBody.class);

    private final String name;
    private final Class<A> argumentsType;
    private final Class<B> bodyType;

    private DapCommandType(String name, Class<A> argumentsType, Class<B> bodyType) {
        this.name = name;
        this.argumentsType = argumentsType;
        this.bodyType = bodyType;
        if (name != null) {
            TYPE_BY_NAME.put(name, this);
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<A> getArgumentsType() {
        return this.argumentsType;
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
        DapCommandType<?, ?> that = (DapCommandType<?, ?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

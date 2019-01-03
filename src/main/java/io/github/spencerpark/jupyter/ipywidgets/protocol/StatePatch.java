package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.BiConsumer;

public class StatePatch {
    public static class Buffer {
        private final String key;
        private final List<byte[]> buffers;

        public Buffer(String key, byte[] buffer) {
            this.key = key;
            this.buffers = Collections.singletonList(buffer);
        }

        public Buffer(String key, List<byte[]> buffers) {
            this.key = key;
            this.buffers = buffers;
        }

        public String getKey() {
            return key;
        }

        public List<byte[]> getBuffers() {
            return buffers;
        }
    }

    private static JsonArray makeBufferPath(String key) {
        JsonArray array = new JsonArray();
        array.add(key);
        return array;
    }

    private static JsonArray makeBufferPath(String key, int index) {
        JsonArray array = new JsonArray();
        array.add(key);
        array.add(index);
        return array;
    }

    private final JsonObject state;
    private Map<String, Buffer> buffers;

    public StatePatch() {
        this.state = new JsonObject();
        this.buffers = new LinkedHashMap<>();
    }

    private void addBuffer(Buffer buffer) {
        if (this.buffers == null)
            this.buffers = new LinkedHashMap<>();

        this.buffers.put(buffer.getKey(), buffer);
    }

    private Buffer getBuffer(String key) {
        if (this.buffers == null)
            return null;

        return this.buffers.get(key);
    }

    public void putBinary(String key, byte[] value) {
        this.addBuffer(new Buffer(key, value));
    }

    public void putBinary(String key, List<byte[]> values) {
        this.addBuffer(new Buffer(key, values));
    }

    public void putJson(String key, JsonElement value) {
        this.state.add(key, value);
    }

    public JsonElement getJson(String key) {
        return this.state.get(key);
    }

    public List<byte[]> getBinary(String key) {
        Buffer buffer = this.getBuffer(key);
        return buffer == null ? null : buffer.getBuffers();
    }

    public void forEachJson(BiConsumer<String, JsonElement> consumer) {
        this.state.entrySet().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    public void forEachBuffer(BiConsumer<String, List<byte[]>> consumer) {
        if (this.buffers == null)
            return;

        this.buffers.forEach((key, buffer) -> consumer.accept(key, buffer.getBuffers()));
    }
}

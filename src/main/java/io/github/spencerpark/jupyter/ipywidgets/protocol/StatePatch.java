package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.function.BiConsumer;

public class StatePatch {
    public enum Opts {
        INCLUDE_ALL,
        CLEAR_DIRTY,
    }

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
    private List<Buffer> buffers;

    public StatePatch() {
        this.state = new JsonObject();
        this.buffers = null;
    }

    protected StatePatch(JsonObject state, JsonArray paths, List<byte[]> buffers) {
        this.state = state;

        if (paths != null && paths.size() > 0) {
            Map<String, List<byte[]>> organizedBuffers = new LinkedHashMap<>();

            Iterator<byte[]> buffersIt = buffers.iterator();
            for (JsonElement e : paths) {
                byte[] buffer = buffersIt.next();

                if (!e.isJsonArray())
                    throw new IllegalArgumentException("'paths' must be an array of arrays but got value: " + e);

                JsonArray path = e.getAsJsonArray();
                switch (path.size()) {
                    case 1:
                        organizedBuffers.put(path.get(0).getAsString(), Collections.singletonList(buffer));
                        break;
                    case 2:
                        String key = path.get(0).getAsString();
                        List<byte[]> buffersForKey = organizedBuffers.computeIfAbsent(key, k -> {
                            int size = state.get(key).getAsJsonArray().size();
                            return Arrays.asList(new byte[size][]);
                        });

                        int index = path.get(1).getAsInt();
                        buffersForKey.set(index, buffer);
                    default:
                        throw new IllegalArgumentException("paths empty or longer than 2 are not supported.");
                }
            }

            this.buffers = new LinkedList<>();
            organizedBuffers.forEach((key, bufs) -> {
                this.state.remove(key);
                this.buffers.add(new Buffer(key, bufs));
            });
        }
    }

    private void addBuffer(Buffer buffer) {
        if (this.buffers == null)
            this.buffers = new LinkedList<>();

        this.buffers.add(buffer);
    }

    private Buffer getBuffer(String key) {
        if (this.buffers == null)
            return null;

        return this.buffers.stream()
                .filter(b -> key.equals(b.key))
                .findFirst()
                .orElse(null);
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

        this.buffers.forEach(buffer -> consumer.accept(buffer.getKey(), buffer.getBuffers()));
    }

    private JsonObject putBufferLandmarks() {
        if (this.buffers != null) {
            this.buffers.forEach(buffer -> {
                int size = buffer.getBuffers().size();
                if (size <= 1) {
                    // Single valued buffers are simply non existent
                    this.state.remove(buffer.getKey());
                } else {
                    // Buffer lists need a list of nulls with the same length
                    JsonArray landmark = new JsonArray();
                    for (int i = 0; i < size; i++)
                        landmark.add(JsonNull.INSTANCE);
                    this.state.add(buffer.getKey(), landmark);
                }
            });
        }

        return this.state;
    }

    protected JsonObject getState() {
        return this.putBufferLandmarks();
    }

    protected JsonArray getBufferPaths() {
        if (this.buffers == null)
            return null;

        JsonArray paths = new JsonArray();
        this.buffers.forEach(buffer -> {
            int size = buffer.getBuffers().size();
            if (size <= 1) {
                JsonArray path = new JsonArray();
                path.add(buffer.getKey());
                paths.add(path);
            } else {
                // Put one with the index for every buffer
                for (int i = 0; i < size; i++) {
                    JsonArray path = new JsonArray();
                    path.add(buffer.getKey());
                    path.add(i);
                    paths.add(path);
                }
            }
        });

        return paths;
    }

    protected List<byte[]> getBuffers() {
        if (this.buffers == null)
            return null;

        List<byte[]> buffers = new LinkedList<>();
        this.buffers.forEach(buffer -> buffers.addAll(buffer.getBuffers()));

        return buffers;
    }
}

package io.github.spencerpark.jupyter.ipywidgets.mock;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetState;

import java.util.*;

public class MockWidgetState implements WidgetState {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Queue<JsonElement> customEvents = new LinkedList<>();

    private final Gson gson;

    public MockWidgetState(Gson gson) {
        this.gson = gson;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public Object getProp(String name) {
        return this.properties.get(name);
    }

    public int getIntProp(String name) {
        return (int) ((double) this.getProp(name));
    }

    public String getStringProp(String name) {
        return String.valueOf(this.getProp(name));
    }

    @Override
    public StatePatch createPatch(EnumSet<StatePatch.Opts> opts) {
        StatePatch patch = new StatePatch();

        this.properties.forEach((name, val) -> {
            if (val instanceof byte[]) {
                patch.putBinary(name, (byte[]) val);
            } else if (val instanceof List) {
                List vals = (List) val;
                if (!vals.isEmpty() && vals.get(0) instanceof byte[])
                    patch.putBinary(name, vals);
                else
                    patch.putJson(name, this.gson.toJsonTree(vals));
            } else {
                patch.putJson(name, this.gson.toJsonTree(val));
            }
        });

        return patch;
    }

    @Override
    public void applyPatch(StatePatch patch) {
        patch.forEachJson((name, element) -> this.properties.put(name, this.gson.fromJson(element, Object.class)));
        patch.forEachBuffer((name, buf) -> {
            if (buf.size() == 1)
                this.properties.put(name, buf.get(0));
            else
                this.properties.put(name, buf);
        });
    }

    @Override
    public void handleCustomContent(JsonElement content) {
        this.customEvents.offer(content);
    }

    public Queue<JsonElement> getCustomEvents() {
        return this.customEvents;
    }
}

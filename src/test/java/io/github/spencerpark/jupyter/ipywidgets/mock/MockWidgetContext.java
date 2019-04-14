package io.github.spencerpark.jupyter.ipywidgets.mock;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MockWidgetContext implements WidgetContext {
    private final Gson gson = WidgetsGson.createInstance(this);
    private final Map<UUID, WidgetPropertyContainer> instances = new LinkedHashMap<>();

    @Override
    public MockRemoteWidgetState connect(WidgetPropertyContainer container) {
        MockRemoteWidgetState remote = new MockRemoteWidgetState(new MockWidgetState(this.gson));
        StatePatch patch = container.createPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL));
        remote.updateState(patch);
        return remote;
    }

    @Override
    public UUID registerInstance(WidgetPropertyContainer instance) {
        UUID id = UUID.randomUUID();
        this.instances.put(id, instance);
        return id;
    }

    @Override
    public void unregisterInstance(WidgetPropertyContainer instance) {
        this.instances.remove(instance.getId());
    }

    @Override
    public WidgetPropertyContainer lookupInstance(UUID id) {
        return this.instances.get(id);
    }

    @Override
    public Gson getSerializer() {
        return this.gson;
    }
}

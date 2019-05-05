package io.github.spencerpark.jupyter.ipywidgets.mock;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MockWidgetContext implements WidgetContext {
    private final Gson gson = WidgetsGson.createInstance(this);
    private final Map<String, WidgetPropertyContainer> instances = new LinkedHashMap<>();

    @Override
    public MockRemoteWidgetState connect(WidgetPropertyContainer container, Consumer<RemoteWidgetState> initialize) {
        MockRemoteWidgetState remote = new MockRemoteWidgetState(new MockWidgetState(this.gson));
        initialize.accept(remote);

        this.instances.put(remote.getId(), container);

        StatePatch patch = container.createPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL));
        remote.updateState(patch);
        return remote;
    }

    @Override
    public WidgetPropertyContainer lookupInstance(String id) {
        return this.instances.get(id);
    }

    @Override
    public Gson getSerializer() {
        return this.gson;
    }
}

package io.github.spencerpark.jupyter.ipywidgets;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetComm;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JupyterWidgetContext implements WidgetContext {
    private final CommManager commManager;

    private final Gson gson = WidgetsGson.createInstance(this);
    private final Map<UUID, WeakReference<WidgetPropertyContainer>> instances = new ConcurrentHashMap<>();


    public JupyterWidgetContext(CommManager commManager) {
        this.commManager = commManager;
    }

    @Override
    public RemoteWidgetState connect(WidgetPropertyContainer container) {
        return commManager.openComm("jupyter.widget", (manager, id, target, openMsg) -> {
            openMsg.getMetadata().put("version", "2.0.0");

            WidgetComm.initializeOpenMessage(openMsg, container.createPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL)));

            return new WidgetComm(manager, id, target, container);
        });
    }

    @Override
    public UUID registerInstance(WidgetPropertyContainer instance) {
        UUID id = UUID.randomUUID();
        this.instances.put(id, new WeakReference<>(instance));
        return id;
    }

    @Override
    public void unregisterInstance(WidgetPropertyContainer instance) {
        this.instances.remove(instance.getId());
    }

    @Override
    public WidgetPropertyContainer lookupInstance(UUID id) {
        WeakReference<WidgetPropertyContainer> ref = instances.get(id);
        if (ref == null)
            return null;

        return ref.get();
    }

    @Override
    public Gson getSerializer() {
        return this.gson;
    }
}

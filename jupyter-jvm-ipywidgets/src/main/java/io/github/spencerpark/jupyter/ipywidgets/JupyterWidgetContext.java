package io.github.spencerpark.jupyter.ipywidgets;

import com.google.gson.Gson;
import io.github.spencerpark.jupyter.ipywidgets.gson.WidgetsGson;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.*;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class JupyterWidgetContext implements WidgetContext {
    public static JupyterWidgetContext install(CommManager commManager) {
        JupyterWidgetContext context = new JupyterWidgetContext(commManager);
        WidgetCommTarget.register(commManager, context);
        return context;
    }

    private final CommManager commManager;

    private final Gson gson = WidgetsGson.createInstance(this);
    private final Map<String, WeakReference<WidgetPropertyContainer>> instances = new ConcurrentHashMap<>();

    public JupyterWidgetContext(CommManager commManager) {
        this.commManager = commManager;
    }

    @Override
    public RemoteWidgetState connect(WidgetPropertyContainer container, Consumer<RemoteWidgetState> initialize) {
        return this.commManager.openComm("jupyter.widget", (manager, id, target, openMsg) -> {
            WidgetComm comm = new WidgetComm(manager, id, target, container);
            initialize.accept(comm);

            this.instances.put(comm.getId(), new WeakReference<>(container));
            comm.onClose(() -> this.instances.remove(comm.getId()));

            openMsg.getNonNullMetadata().put("version", "2.0.0");
            WidgetComm.initializeOpenMessage(openMsg, container.createPatch(EnumSet.of(StatePatch.Opts.INCLUDE_ALL)));

            return comm;
        });
    }

    @Override
    public WidgetPropertyContainer lookupInstance(String id) {
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

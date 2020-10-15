package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.api.comm.Comm;
import io.github.spencerpark.jupyter.api.comm.CommManager;
import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.api.comm.CommTarget;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;

public class WidgetCommTarget implements CommTarget {
    public static void register(CommManager manager, WidgetContext context) {
        manager.registerTarget("jupyter.widget", new WidgetCommTarget(context));
    }

    private final WidgetContext context;

    public WidgetCommTarget(WidgetContext context) {
        this.context = context;
    }

    @Override
    public Comm createComm(CommManager commManager, String id, String targetName, CommMessage.Open msg) {
        JsonObject payload = msg.getData().getAsJsonObject();

        String widgetProtocolVersion = (String) msg.getMetadata().get("version");

        JsonObject state = payload.get("state").getAsJsonObject();
        JsonElement bufferPaths = payload.get("buffer_paths");

        WidgetCoordinates coords = this.context.getSerializer().fromJson(state, WidgetCoordinates.class);
        WidgetPropertyContainer container = WidgetPropertyContainer.instantiate(coords, this.context);

        WidgetComm comm = new WidgetComm(commManager, id, targetName, container);

        comm.receiveUpdate(state, bufferPaths == null ? null : bufferPaths.getAsJsonArray(), msg.getBlobs());

        return comm;
    }
}

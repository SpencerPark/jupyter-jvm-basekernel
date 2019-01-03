package io.github.spencerpark.jupyter.ipywidgets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.kernel.comm.Comm;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;
import io.github.spencerpark.jupyter.kernel.comm.CommTarget;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommOpenCommand;

public class WidgetCommTarget implements CommTarget {
    public static void register(CommManager manager) {
        manager.registerTarget("jupyter.widget", new WidgetCommTarget());
    }

    @Override
    public Comm createComm(CommManager commManager, String id, String targetName, Message<CommOpenCommand> msg) {
        WidgetComm comm = new WidgetComm(commManager, id, targetName);

        CommOpenCommand openCommand = msg.getContent();
        JsonObject data = openCommand.getData();

        String widgetProtocolVersion = (String) msg.getMetadata().get("version");

        data.get("state").getAsJsonObject();
        data.get("buffer_paths").getAsJsonArray();

        return comm;
    }
}

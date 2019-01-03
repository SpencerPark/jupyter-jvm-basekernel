package io.github.spencerpark.jupyter.ipywidgets;

import io.github.spencerpark.jupyter.kernel.comm.Comm;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommCloseCommand;
import io.github.spencerpark.jupyter.messages.comm.CommMsgCommand;

public class WidgetComm extends Comm {
    public WidgetComm(CommManager manager, String id, String targetName) {
        super(manager, id, targetName);
    }

    @Override
    protected void onMessage(Message<CommMsgCommand> message) {

    }

    @Override
    protected void onClose(Message<CommCloseCommand> closeMessage, boolean sending) {

    }
}

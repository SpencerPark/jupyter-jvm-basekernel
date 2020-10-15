package io.github.spencerpark.jupyter.comm;

import io.github.spencerpark.jupyter.api.comm.Comm;
import io.github.spencerpark.jupyter.api.comm.CommTarget;
import io.github.spencerpark.jupyter.channels.ReplyEnvironment;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.comm.CommCloseCommand;
import io.github.spencerpark.jupyter.messages.comm.CommMsgCommand;
import io.github.spencerpark.jupyter.messages.comm.CommOpenCommand;
import io.github.spencerpark.jupyter.messages.reply.CommInfoReply;
import io.github.spencerpark.jupyter.messages.request.CommInfoRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultCommServer {
    private final DefaultCommManager comms;
    private final CommClient client;

    public DefaultCommServer(DefaultCommManager comms, CommClient client) {
        this.comms = comms;
        this.client = client;
    }

    // The shell handler for the kernel side implementation.
    public void handleCommOpenCommand(ReplyEnvironment env, Message<CommOpenCommand> commOpenCommandMessage) {
        env.setBusyDeferIdle();

        this.handleCommOpenCommand(commOpenCommandMessage);
    }

    // The iopub handler for the client side implementation.
    public void handleCommOpenCommand(Message<CommOpenCommand> commOpenCommandMessage) {
        CommOpenCommand openCommand = commOpenCommandMessage.getContent();

        CommTarget target = this.comms.getTarget(openCommand.getTargetName());
        if (target == null) {
            this.client.sendClose(commOpenCommandMessage, openCommand.getCommID(), commOpenCommandMessage.getContent().getData(), msg -> {});
        } else {
            Comm comm = target.createComm(this.comms, openCommand.getCommID(), openCommand.getTargetName(), new CommOpenMessageAdapter(commOpenCommandMessage));
            this.comms.registerComm(comm);
        }
    }

    public void handleCommMsgCommand(ReplyEnvironment env, Message<CommMsgCommand> commMsgCommandMessage) {
        env.setBusyDeferIdle();

        this.handleCommMsgCommand(commMsgCommandMessage);
    }

    public void handleCommMsgCommand(Message<CommMsgCommand> commMsgCommandMessage) {
        CommMsgCommand msgCommand = commMsgCommandMessage.getContent();

        Comm comm = this.comms.getCommByID(msgCommand.getCommID());
        if (comm != null) {
            comm.onMessage(new CommDataMessageAdapter(commMsgCommandMessage));
        }
    }

    public void handleCommCloseCommand(ReplyEnvironment env, Message<CommCloseCommand> commCloseCommandMessage) {
        env.setBusyDeferIdle();

        this.handleCommCloseCommand(commCloseCommandMessage);
    }

    public void handleCommCloseCommand(Message<CommCloseCommand> commCloseCommandMessage) {
        CommCloseCommand closeCommand = commCloseCommandMessage.getContent();

        Comm comm = this.comms.unregisterComm(closeCommand.getCommID());
        if (comm != null) {
            comm.onClose(new CommCloseMessageAdapter(commCloseCommandMessage), false);
        }
    }

    // This is a request, not a command. It will only come over the shell to the kernel and not vice-versa.
    public void handleCommInfoRequest(ReplyEnvironment env, Message<CommInfoRequest> commInfoRequestMessage) {
        CommInfoRequest request = commInfoRequestMessage.getContent();

        env.setBusyDeferIdle();

        Map<String, CommInfoReply.CommInfo> comms = new LinkedHashMap<>();

        String targetNameFilter = request.getTargetName();
        if (targetNameFilter != null) {
            this.comms.forEach(comm -> {
                if (targetNameFilter.equals(comm.getTargetName()))
                    comms.put(comm.getID(), new CommInfoReply.CommInfo(comm.getTargetName()));
            });
        } else {
            this.comms.forEach(comm -> comms.put(comm.getID(), new CommInfoReply.CommInfo(comm.getTargetName())));
        }

        env.reply(new CommInfoReply(comms));
    }
}

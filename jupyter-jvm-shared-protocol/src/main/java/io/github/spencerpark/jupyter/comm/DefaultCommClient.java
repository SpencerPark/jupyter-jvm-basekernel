package io.github.spencerpark.jupyter.comm;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageContext;
import io.github.spencerpark.jupyter.messages.comm.CommCloseCommand;
import io.github.spencerpark.jupyter.messages.comm.CommMsgCommand;
import io.github.spencerpark.jupyter.messages.comm.CommOpenCommand;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultCommClient implements CommClient {
    private final JupyterSocket transport;

    public DefaultCommClient(JupyterSocket transport) {
        this.transport = transport;
    }

    @Override
    public <T> T sendOpen(MessageContext context, String commID, String target, JsonObject data, Function<CommMessage.Open, T> configure) {
        CommOpenCommand content = new CommOpenCommand(commID, target, data == null ? new JsonObject() : data);
        Message<CommOpenCommand> message = new Message<>(context, CommOpenCommand.MESSAGE_TYPE, content);

        T result = configure.apply(new CommOpenMessageAdapter(message));

        this.transport.sendMessage(message);

        return result;
    }

    @Override
    public void sendMessage(MessageContext context, String commID, JsonObject data, Map<String, Object> metadata, List<byte[]> blobs) {
        CommMsgCommand content = new CommMsgCommand(commID, data == null ? new JsonObject() : data);
        Message<CommMsgCommand> message = new Message<>(context, CommMsgCommand.MESSAGE_TYPE, content, blobs, metadata);

        this.transport.sendMessage(message);
    }

    @Override
    public void sendClose(MessageContext context, String commID, JsonObject data, Consumer<CommMessage.Close> configure) {
        CommCloseCommand content = new CommCloseCommand(commID, data == null ? new JsonObject() : data);
        Message<CommCloseCommand> message = new Message<>(context, CommCloseCommand.MESSAGE_TYPE, content);

        configure.accept(new CommCloseMessageAdapter(message));

        this.transport.sendMessage(message);
    }
}

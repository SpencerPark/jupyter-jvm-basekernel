package io.github.spencerpark.jupyter.comm;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.api.comm.CommMessage;
import io.github.spencerpark.jupyter.messages.MessageContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CommClient {
    public <T> T sendOpen(MessageContext context, String commID, String target, JsonObject data, Function<CommMessage.Open, T> configure);

    public void sendMessage(MessageContext context, String commID, JsonObject data, Map<String, Object> metadata, List<byte[]> blobs);

    public void sendClose(MessageContext context, String commID, JsonObject data, Consumer<CommMessage.Close> configure);
}

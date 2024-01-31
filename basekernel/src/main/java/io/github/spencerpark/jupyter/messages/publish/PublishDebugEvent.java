package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.adapters.JsonInline;
import io.github.spencerpark.jupyter.messages.debug.DapEvent;

public class PublishDebugEvent implements ContentType<PublishDebugEvent> {
    public static final MessageType<PublishDebugEvent> MESSAGE_TYPE = MessageType.PUBLISH_DEBUG_EVENT;

    @Override
    public MessageType<PublishDebugEvent> getType() {
        return MESSAGE_TYPE;
    }

    @JsonInline
    protected final DapEvent<JsonObject> dapEvent;

    public PublishDebugEvent(DapEvent<JsonObject> dapEvent) {
        this.dapEvent = dapEvent;
    }

    public DapEvent<JsonObject> getDapEvent() {
        return dapEvent;
    }
}

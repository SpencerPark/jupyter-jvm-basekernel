package io.github.spencerpark.jupyter.messages.publish;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;

public class PublishDebugEvent implements JsonBox, ContentType<PublishDebugEvent> {
    public static final MessageType<PublishDebugEvent> MESSAGE_TYPE = MessageType.PUBLISH_DEBUG_EVENT;

    @Override
    public MessageType<PublishDebugEvent> getType() {
        return MESSAGE_TYPE;
    }

    protected final JsonBox.Wrapper dapEvent;

    public PublishDebugEvent(JsonBox.Wrapper dapEvent) {
        this.dapEvent = dapEvent;
    }

    @JsonBox.Unboxer
    public JsonBox.Wrapper getDapEvent() {
        return dapEvent;
    }
}

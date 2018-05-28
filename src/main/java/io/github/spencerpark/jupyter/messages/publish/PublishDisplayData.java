package io.github.spencerpark.jupyter.messages.publish;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.ExpressionValue;
import io.github.spencerpark.jupyter.messages.MessageType;

public class PublishDisplayData extends DisplayData implements ExpressionValue, ContentType<PublishDisplayData> {
    public static final MessageType<PublishDisplayData> MESSAGE_TYPE = MessageType.PUBLISH_DISPLAY_DATA;

    @Override
    public MessageType<PublishDisplayData> getType() {
        return MESSAGE_TYPE;
    }

    public PublishDisplayData(DisplayData data) {
        super(data);
    }
}

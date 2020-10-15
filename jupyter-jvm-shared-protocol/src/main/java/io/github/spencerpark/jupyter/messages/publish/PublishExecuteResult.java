package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.messages.MessageType;

public class PublishExecuteResult extends DisplayData implements ContentType<PublishExecuteResult> {
    public static final MessageType<PublishExecuteResult> MESSAGE_TYPE = MessageType.PUBLISH_EXECUTION_RESULT;

    @Override
    public MessageType<PublishExecuteResult> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("execution_count")
    private final int count;

    public PublishExecuteResult(int count, DisplayData data) {
        super(data);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

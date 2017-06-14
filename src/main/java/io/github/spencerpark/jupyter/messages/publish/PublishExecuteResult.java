package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MIMEBundle;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.Map;

public class PublishExecuteResult implements ContentType<PublishExecuteResult> {
    public static final MessageType<PublishExecuteResult> MESSAGE_TYPE = MessageType.PUBLISH_EXECUTION_RESULT;

    @Override
    public MessageType<PublishExecuteResult> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("execution_count")
    private final int count;

    /**
     * MIME type to value for the various representations of the output
     * data.
     */
    protected final MIMEBundle data;

    /**
     * Any additional metadata. IPython supports:
     * {@code 'image/png': { width: number, height: number }}
     * and
     * {@code 'application/json': {expanded: boolean}}
     */
    protected final Map<String, Object> metadata;

    public PublishExecuteResult(int count, MIMEBundle data, Map<String, Object> metadata) {
        this.count = count;
        this.data = data;
        this.metadata = metadata;
    }

    public int getCount() {
        return count;
    }

    public MIMEBundle getData() {
        return data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}

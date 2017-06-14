package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ExpressionValue;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MIMEBundle;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.LinkedHashMap;
import java.util.Map;

public class PublishDisplayData implements ExpressionValue, ContentType<PublishDisplayData> {
    public static final MessageType<PublishDisplayData> MESSAGE_TYPE = MessageType.PUBLISH_DISPLAY_DATA;

    @Override
    public MessageType<PublishDisplayData> getType() {
        return MESSAGE_TYPE;
    }

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

    /**
     * Like {@link #data} but this data should not be persisted throughout
     * saving, exporting, etc.
     */
    @SerializedName("transient")
    protected final MIMEBundle transientData;

    public PublishDisplayData() {
        this.data = new MIMEBundle();
        this.metadata = new LinkedHashMap<>();
        this.transientData = new MIMEBundle();
    }

    public void putData(String mimeType, Object value) {
        this.putData(mimeType, value, false);
    }

    public void putData(String mimeType, Object value, boolean isTransient) {
        (isTransient ? this.transientData : this.data).put(mimeType, value);
    }

    public void putMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public MIMEBundle getData() {
        return data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public MIMEBundle getTransientData() {
        return transientData;
    }
}

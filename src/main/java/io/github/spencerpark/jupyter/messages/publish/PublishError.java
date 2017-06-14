package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

/**
 * See also {@link ErrorReply}
 */
public class PublishError implements ContentType<PublishError> {
    public static final MessageType<PublishError> MESSAGE_TYPE = MessageType.PUBLISH_ERROR;

    @Override
    public MessageType<PublishError> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("execution_count")
    private final int count;

    public PublishError(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

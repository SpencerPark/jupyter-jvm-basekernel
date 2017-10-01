package io.github.spencerpark.jupyter.messages.publish;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See also {@link ErrorReply}
 */
public class PublishError implements ContentType<PublishError> {
    public static final MessageType<PublishError> MESSAGE_TYPE = MessageType.PUBLISH_ERROR;

    public static PublishError of(Exception exception, ErrorFormatter formatter) {
        String name = exception.getClass().getSimpleName();
        String msg = exception.getLocalizedMessage();
        List<String> stacktrace = formatter.format(exception);

        return new PublishError(name, msg, stacktrace);
    }

    @Override
    public MessageType<PublishError> getType() {
        return MESSAGE_TYPE;
    }

    @SerializedName("ename")
    protected final String errName;
    @SerializedName("evalue")
    protected final String errMsg;
    @SerializedName("traceback")
    protected final List<String> stacktrace;

    public PublishError(String errName, String errMsg, List<String> stacktrace) {
        this.errName = errName;
        this.errMsg = errMsg;
        this.stacktrace = stacktrace;
    }

    public String getErrorName() {
        return errName;
    }

    public String getErrorMessage() {
        return errMsg;
    }

    public List<String> getStacktrace() {
        return stacktrace;
    }
}

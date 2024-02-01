package io.github.spencerpark.jupyter.messages.reply;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.kernel.ExpressionValue;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.publish.PublishDisplayData;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.request.ExecuteRequest;

import java.util.Map;

public class ExecuteReply implements ContentType<ExecuteReply>, ReplyType<ExecuteRequest> {
    public static final MessageType<ExecuteReply> MESSAGE_TYPE = MessageType.EXECUTE_REPLY;
    public static final MessageType<ExecuteRequest> REQUEST_MESSAGE_TYPE = MessageType.EXECUTE_REQUEST;

    @Override
    public MessageType<ExecuteReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<ExecuteRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    public enum Status {
        @SerializedName("ok") OK,
        @SerializedName("error") ERROR
    }

    private final Status status;

    @SerializedName("execution_count")
    protected final int executionCount;

    /**
     * The values are either {@link ErrorReply} or {@link PublishDisplayData}
     */
    @SerializedName("user_expressions")
    protected final Map<String, ExpressionValue> evaluatedUserExpr;

    public ExecuteReply(int executionCount, Map<String, ExpressionValue> evaluatedUserExpr) {
        this.status = Status.OK;
        this.executionCount = executionCount;
        this.evaluatedUserExpr = evaluatedUserExpr;
    }

    public ExecuteReply(int executionCount) {
        this.status = Status.ERROR;
        this.executionCount = executionCount;
        this.evaluatedUserExpr = null;
    }

    public Status getStatus() {
        return status;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public Map<String, ExpressionValue> getEvaluatedUserExpr() {
        return evaluatedUserExpr;
    }
}

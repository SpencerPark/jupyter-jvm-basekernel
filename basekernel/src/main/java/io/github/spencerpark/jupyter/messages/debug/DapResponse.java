package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

public final class DapResponse<A, B> extends DapProtocolMessage<DapResponse<A, B>> {
    public static final class ErrorBody {
        @SerializedName("error")
        private final DapErrorMessage error;

        public ErrorBody(DapErrorMessage error) {
            this.error = error;
        }

        public DapErrorMessage getError() {
            return error;
        }
    }

    @SerializedName("request_seq")
    private final int requestSeq;

    @SerializedName("command")
    private final DapCommandType<A, B> command;

    // If true the payload is in `body`, otherwise the `body` is a serialized error.
    @SerializedName("success")
    private final boolean success;

    // Optional
    // Error code if `!success`, null otherwise. e.g. 'cancelled' and 'notStopped' are listed in the spec.
    @SerializedName("message")
    private final String message;

    // Optional
    @SerializedName("body")
    private final Object body;

    public static <A, B> DapResponse<A, B> error(int seq, int requestSeq, DapCommandType<A, B> command, String message, DapErrorMessage error) {
        return new DapResponse<>(seq, requestSeq, command, false, message, new ErrorBody(error));
    }

    public static <A, B> DapResponse<A, B> success(int seq, int requestSeq, DapCommandType<A, B> command, B body) {
        return new DapResponse<>(seq, requestSeq, command, true, null, body);
    }

    public DapResponse(int seq, int requestSeq, DapCommandType<A, B> command, boolean success, String message, Object body) {
        super(seq, Type.RESPONSE);
        this.requestSeq = requestSeq;
        this.command = command;
        this.success = success;
        this.message = message;
        this.body = body;
    }

    @Override
    public DapResponse<A, B> withSeq(int seq) {
        return new DapResponse<>(seq, this.requestSeq, this.command, this.success, this.message, this.body);
    }

    public int getRequestSeq() {
        return requestSeq;
    }

    public DapCommandType<A, B> getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    public B getSuccessfulBody() {
        if (this.success) {
            return (B) this.body;
        } else {
            throw new IllegalStateException("Response is not successful, body is an error. Check with isSuccess() before calling this method");
        }
    }

    public DapErrorMessage getUnsuccessfulBody() {
        if (!this.success) {
            return ((ErrorBody) this.body).getError();
        } else {
            throw new IllegalStateException("Response is not successful, body is not an error. Check with !isSuccess() before calling this method");
        }
    }
}

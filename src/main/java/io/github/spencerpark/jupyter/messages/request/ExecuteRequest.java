package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.MessageType;

import java.util.Map;

public class ExecuteRequest {
    public static final MessageType<ExecuteRequest> MESSAGE_TYPE = MessageType.EXECUTE_REQUEST;

    /**
     * The source code to execute. May be a multiline string.
     */
    protected final String code;

    /**
     *  silent -> !store_history
     *
     *  if silent:
     *      - no broadcast on IOPUB channel
     *      - no execute_result reply
     *
     *  Default: {@code false}
     */
    protected final boolean silent;

    /**
     * if storeHistory:
     *     - populate history
     */
    @SerializedName("store_history")
    protected final boolean storeHistory;

    /**
     * A bank of {@code name -> code} that need to be evaluated.
     *
     * The idea behind it is that a front end may always want {@code path -> `pwd`}
     * so that they can display where the kernel is.
     */
    @SerializedName("user_expressions")
    protected final Map<String, String> userExpr;

    @SerializedName("allow_stdin")
    protected final boolean stdinEnabled;

    @SerializedName("stop_on_error")
    protected final boolean stopOnError;

    public ExecuteRequest(String code, boolean silent, boolean storeHistory, Map<String, String> userExpr, boolean stdinEnabled, boolean stopOnError) {
        this.code = code;
        this.silent = silent;
        this.storeHistory = storeHistory;
        this.userExpr = userExpr;
        this.stdinEnabled = stdinEnabled;
        this.stopOnError = stopOnError;
    }

    public String getCode() {
        return code;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean shouldStoreHistory() {
        return storeHistory;
    }

    public Map<String, String> getUserExpr() {
        return userExpr;
    }

    public boolean isStdinEnabled() {
        return stdinEnabled;
    }

    public boolean shouldStopOnError() {
        return stopOnError;
    }

    @Override
    public String toString() {
        return "ExecuteRequest{" +
                "code='" + code + '\'' +
                ", silent=" + silent +
                ", storeHistory=" + storeHistory +
                ", userExpr=" + userExpr +
                ", stdinEnabled=" + stdinEnabled +
                ", stopOnError=" + stopOnError +
                '}';
    }
}

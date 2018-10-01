package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.MessageType;

public class CompleteRequest {
    public static final MessageType<CompleteRequest> MESSAGE_TYPE = MessageType.COMPLETE_REQUEST;

    protected final String code;

    @SerializedName("cursor_pos")
    protected final int cursorPos;

    public CompleteRequest(String code, int cursorPos) {
        this.code = code;
        this.cursorPos = cursorPos;
    }

    public String getCode() {
        return code;
    }

    public int getCursorPos() {
        return cursorPos;
    }
}

package io.github.spencerpark.jupyter.messages.debug.arguments;

import com.google.gson.annotations.SerializedName;

public class RichInspectVariablesArguments {
    @SerializedName("variableName")
    protected final String variableName;

    // Optional, present when a breakpoint is hit
    @SerializedName("frameId")
    protected final Integer frameId;

    public RichInspectVariablesArguments(String variableName, Integer frameId) {
        this.variableName = variableName;
        this.frameId = frameId;
    }

    public String getVariableName() {
        return variableName;
    }

    public Integer getFrameId() {
        return frameId;
    }
}

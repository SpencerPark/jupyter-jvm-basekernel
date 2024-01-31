package io.github.spencerpark.jupyter.messages.debug.arguments;

import com.google.gson.annotations.SerializedName;

public class CopyToGlobalsArguments {
    @SerializedName("srcVariableName")
    protected final String srcVariableName;

    @SerializedName("srcFrameId")
    protected final int srcFrameId;

    @SerializedName("dstVariableName")
    protected final String dstVariableName;

    public CopyToGlobalsArguments(String srcVariableName, int srcFrameId, String dstVariableName) {
        this.srcVariableName = srcVariableName;
        this.srcFrameId = srcFrameId;
        this.dstVariableName = dstVariableName;
    }

    public String getSrcVariableName() {
        return srcVariableName;
    }

    public int getSrcFrameId() {
        return srcFrameId;
    }

    public String getDstVariableName() {
        return dstVariableName;
    }
}

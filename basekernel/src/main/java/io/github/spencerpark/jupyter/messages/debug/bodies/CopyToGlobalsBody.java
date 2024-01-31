package io.github.spencerpark.jupyter.messages.debug.bodies;

import com.google.gson.annotations.SerializedName;

public class CopyToGlobalsBody {
    @SerializedName("value")
    protected final String value;

    @SerializedName("type")
    protected final String type;

    @SerializedName("variablesReference")
    protected final int variablesReference;

    public CopyToGlobalsBody(String value, String type, int variablesReference) {
        this.value = value;
        this.type = type;
        this.variablesReference = variablesReference;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public int getVariablesReference() {
        return variablesReference;
    }
}

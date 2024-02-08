package io.github.spencerpark.jupyter.messages.debug.arguments;

import com.google.gson.annotations.SerializedName;

public class DumpCellArguments {
    @SerializedName("code")
    protected final String code;

    public DumpCellArguments(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

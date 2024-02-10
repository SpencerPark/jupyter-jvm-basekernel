package io.github.spencerpark.jupyter.messages.debug.bodies;

import com.google.gson.annotations.SerializedName;

public class DumpCellBody {
    @SerializedName("sourcePath")
    protected final String sourcePath;

    public DumpCellBody(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourcePath() {
        return sourcePath;
    }
}

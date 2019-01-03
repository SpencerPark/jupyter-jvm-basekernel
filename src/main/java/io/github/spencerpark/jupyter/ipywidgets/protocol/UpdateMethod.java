package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateMethod extends WidgetMethod {
    private Object state;

    @SerializedName("buffer_paths")
    private List<List<CharSequence>> bufferPaths = null;

    public UpdateMethod() {
        super(WidgetMethodType.UPDATE);
    }

    public Object getState() {
        return state;
    }

    public List<List<CharSequence>> getBufferPaths() {
        return bufferPaths;
    }
}

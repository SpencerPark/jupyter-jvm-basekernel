package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.annotations.SerializedName;

public enum WidgetMethodType {
    @SerializedName("update")
    UPDATE,

    @SerializedName("request_state")
    REQUEST_STATE,

    @SerializedName("custom")
    CUSTOM
}

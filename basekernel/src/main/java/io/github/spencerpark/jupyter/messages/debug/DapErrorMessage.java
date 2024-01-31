package io.github.spencerpark.jupyter.messages.debug;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public final class DapErrorMessage {
    @SerializedName("id")
    private final int id;

    @SerializedName("format")
    private final String format;

    // Optional
    @SerializedName("variables")
    private final Map<String, String> variables;

    // Optional
    @SerializedName("sendTelemetry")
    private final Boolean sendTelemetry;

    // Optional
    @SerializedName("showUser")
    private final Boolean showUser;

    // Optional
    @SerializedName("url")
    private final String url;

    @SerializedName("urlLabel")
    private final String urlLabel;

    public DapErrorMessage(int id, String format, Map<String, String> variables, Boolean sendTelemetry, Boolean showUser, String url, String urlLabel) {
        this.id = id;
        this.format = format;
        this.variables = variables;
        this.sendTelemetry = sendTelemetry;
        this.showUser = showUser;
        this.url = url;
        this.urlLabel = urlLabel;
    }

    public int getId() {
        return id;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Boolean getSendTelemetry() {
        return sendTelemetry;
    }

    public Boolean getShowUser() {
        return showUser;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlLabel() {
        return urlLabel;
    }
}

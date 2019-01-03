package io.github.spencerpark.jupyter.ipywidgets.protocol;

public abstract class WidgetMethod {
    private WidgetMethodType method;

    public WidgetMethod() {
    }

    public WidgetMethod(WidgetMethodType method) {
        this.method = method;
    }

    public WidgetMethodType getMethod() {
        return method;
    }

    public void setMethod(WidgetMethodType method) {
        this.method = method;
    }
}

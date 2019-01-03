package io.github.spencerpark.jupyter.ipywidgets.protocol;

public class CustomMethod extends WidgetMethod {
    private Object content;

    public CustomMethod() {
        super(WidgetMethodType.CUSTOM);
    }

    public Object getContent() {
        return content;
    }
}

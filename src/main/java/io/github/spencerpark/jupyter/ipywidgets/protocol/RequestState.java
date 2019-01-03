package io.github.spencerpark.jupyter.ipywidgets.protocol;

public class RequestState extends WidgetMethod {
    public static final RequestState INSTANCE = new RequestState();

    public RequestState() {
        super(WidgetMethodType.REQUEST_STATE);
    }
}

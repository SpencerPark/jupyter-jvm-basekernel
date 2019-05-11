package io.github.spencerpark.jupyter.ipywidgets.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

public class WidgetsGson {
    public static GsonBuilder builder(WidgetContext context) {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(InlineTypeAdapter.FACTORY)
                .registerTypeHierarchyAdapter(WidgetPropertyContainer.class, new WidgetPropertyContainerTypeAdapter(context));
    }

    public static Gson createInstance(WidgetContext context) {
        return builder(context).create();
    }
}

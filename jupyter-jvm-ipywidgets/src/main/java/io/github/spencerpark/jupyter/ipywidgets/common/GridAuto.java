package io.github.spencerpark.jupyter.ipywidgets.common;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

public class GridAuto extends WidgetPropertyContainer {
    public enum GridAutoFlow {
        @SerializedName("column") COLUMN,
        @SerializedName("row") ROW,
        @SerializedName("row dense") ROW_DENSE,
        @SerializedName("column dense") COLUMN_DENSE,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public GridAuto(WidgetContext context) {
        super(context);
    }

    public final WidgetProperty<String> columns = super.property("columns", String.class);
    public final WidgetProperty<GridAutoFlow> flow = super.property("flow", GridAutoFlow.class);
    public final WidgetProperty<String> rows = super.property("rows", String.class);

}

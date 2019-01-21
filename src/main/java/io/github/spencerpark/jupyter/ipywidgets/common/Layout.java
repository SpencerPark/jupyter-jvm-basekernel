package io.github.spencerpark.jupyter.ipywidgets.common;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetCoordinates;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetProperty;
import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;

public class Layout extends WidgetPropertyContainer {
    public static final WidgetCoordinates COORDS = register(
            Layout::new,
            WidgetCoordinates.BASE.with(b -> {
                b.model.name("LayoutModel");
                b.view.name("LayoutView").module("@jupyter-widgets/base").version(ProtocolConstants.JUPYTER_WIDGETS_BASE_VERSION);
            })
    );

    public enum AlignContent {
        @SerializedName("flex-start") FLEX_START,
        @SerializedName("flex-end") FLEX_END,
        @SerializedName("center") CENTER,
        @SerializedName("space-between") SPACE_BETWEEN,
        @SerializedName("space-around") SPACE_AROUND,
        @SerializedName("space-evenly") SPACE_EVENLY,
        @SerializedName("stretch") STRETCH,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public enum AlignItems {
        @SerializedName("flex-start") FLEX_START,
        @SerializedName("flex-end") FLEX_END,
        @SerializedName("center") CENTER,
        @SerializedName("baseline") BASELINE,
        @SerializedName("stretch") STRETCH,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public enum AlignSelf {
        @SerializedName("auto") AUTO,
        @SerializedName("flex-start") FLEX_START,
        @SerializedName("flex-end") FLEX_END,
        @SerializedName("center") CENTER,
        @SerializedName("baseline") BASELINE,
        @SerializedName("stretch") STRETCH,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public enum JustifyContent {
        @SerializedName("flex-start") FLEX_START,
        @SerializedName("flex-end") FLEX_END,
        @SerializedName("center") CENTER,
        @SerializedName("space-between") SPACE_BETWEEN,
        @SerializedName("space-around") SPACE_AROUND,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public enum Overflow {
        @SerializedName("visible") VISIBLE,
        @SerializedName("hidden") HIDDEN,
        @SerializedName("scroll") SCROLL,
        @SerializedName("auto") AUTO,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public enum Visibility {
        @SerializedName("visible") VISIBLE,
        @SerializedName("hidden") HIDDEN,
        @SerializedName("inherit") INHERIT,
        @SerializedName("initial") INITIAL,
        @SerializedName("unset") UNSET
    }

    public final WidgetProperty<AlignContent> alignContent = super.property("align_content", AlignContent.class);
    public final WidgetProperty<AlignItems> alignItems = super.property("align_items", AlignItems.class);
    public final WidgetProperty<AlignSelf> alignSelf = super.property("align_self", AlignSelf.class);
    public final WidgetProperty<String> bottom = super.property("bottom", String.class);
    public final WidgetProperty<String> border = super.property("border", String.class);
    public final WidgetProperty<String> display = super.property("display", String.class);
    public final WidgetProperty<String> flex = super.property("flex", String.class);
    public final WidgetProperty<String> flexFlow = super.property("flex_flow", String.class);
    public final WidgetProperty<String> height = super.property("height", String.class);
    public final WidgetProperty<JustifyContent> justifyContent = super.property("justify_content", JustifyContent.class);
    public final WidgetProperty<String> left = super.property("left", String.class);
    public final WidgetProperty<String> margin = super.property("margin", String.class);
    public final WidgetProperty<String> maxHeight = super.property("max_height", String.class);
    public final WidgetProperty<String> maxWidth = super.property("max_width", String.class);
    public final WidgetProperty<String> minHeight = super.property("min_height", String.class);
    public final WidgetProperty<String> minWidth = super.property("min_width", String.class);

    public final WidgetProperty<Overflow> overflow = super.property("overflow", Overflow.class);
    public final WidgetProperty<Overflow> overflowX = super.property("overflow_x", Overflow.class);
    public final WidgetProperty<Overflow> overflowY = super.property("overflow_y", Overflow.class);

    public final WidgetProperty<String> order = super.property("order", String.class);
    public final WidgetProperty<String> padding = super.property("padding", String.class);
    public final WidgetProperty<String> right = super.property("right", String.class);
    public final WidgetProperty<String> top = super.property("top", String.class);
    public final WidgetProperty<Visibility> visibility = super.property("visibility", Visibility.class);
    public final WidgetProperty<String> width = super.property("width", String.class);

    public final Grid grid = super.inline("grid_", new Grid());
}

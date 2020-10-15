package io.github.spencerpark.jupyter.ipywidgets.props;

import io.github.spencerpark.jupyter.ipywidgets.gson.JsonInline;
import io.github.spencerpark.jupyter.ipywidgets.protocol.ProtocolConstants;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO WidgetCoordinates<T> for the type that is instantiated
public class WidgetCoordinates {
    public static class Builder {
        public final ComponentCoordinates.Builder model = ComponentCoordinates.builder();
        public final ComponentCoordinates.Builder view = ComponentCoordinates.builder();

        public Builder model(Consumer<ComponentCoordinates.Builder> configure) {
            configure.accept(this.model);
            return this;
        }

        public Builder view(Consumer<ComponentCoordinates.Builder> configure) {
            configure.accept(this.view);
            return this;
        }

        public WidgetCoordinates create() {
            return WidgetCoordinates.of(this.model.create(), this.view.create());
        }
    }

    public static WidgetCoordinates.Builder builder() {
        return new Builder();
    }

    public static WidgetCoordinates of(ComponentCoordinates model, ComponentCoordinates view) {
        return new WidgetCoordinates(model, view);
    }

    public static final WidgetCoordinates BASE = WidgetCoordinates.builder()
            .model(model ->
                    model.name("WidgetModel")
                            .module("@jupyter-widgets/base")
                            .version(ProtocolConstants.JUPYTER_WIDGETS_BASE_VERSION))
            .view(view -> view.version(""))
            .create();

    @JsonInline("_model_{0}")
    public final ComponentCoordinates model;

    @JsonInline("_view_{0}")
    public final ComponentCoordinates view;

    private WidgetCoordinates(ComponentCoordinates model, ComponentCoordinates view) {
        this.model = model;
        this.view = view;
    }

    public WidgetCoordinates with(Consumer<WidgetCoordinates.Builder> configure) {
        WidgetCoordinates.Builder builder = WidgetCoordinates.builder();
        builder.model
                .name(this.model.getName())
                .module(this.model.getModule())
                .version(this.model.getVersion());
        builder.view
                .name(this.view.getName())
                .module(this.view.getModule())
                .version(this.view.getVersion());

        configure.accept(builder);
        return builder.create();
    }

    public WidgetCoordinates with(BiConsumer<ComponentCoordinates.Builder, ComponentCoordinates.Builder> configure) {
        WidgetCoordinates.Builder builder = WidgetCoordinates.builder();
        builder.model
                .name(this.model.getName())
                .module(this.model.getModule())
                .version(this.model.getVersion());
        builder.view
                .name(this.view.getName())
                .module(this.view.getModule())
                .version(this.view.getVersion());

        configure.accept(builder.model, builder.view);
        return builder.create();
    }
}

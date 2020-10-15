package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.display.RenderContext;
import io.github.spencerpark.jupyter.api.display.Renderer;
import io.github.spencerpark.jupyter.api.display.mime.MIMEType;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultRenderContext implements RenderContext {
    private final RenderRequestTypes requestedTypes;
    private final Renderer renderer;
    private final Map<String, Object> params;
    private final DisplayData out;

    public DefaultRenderContext(RenderRequestTypes requestedTypes, Renderer renderer, Map<String, Object> params, DisplayData out) {
        this.requestedTypes = requestedTypes;
        this.renderer = renderer;
        this.params = params;
        this.out = out;
    }

    @Override
    public Renderer getRenderer() {
        return this.renderer;
    }

    @Override
    public DisplayData getOutputContainer() {
        return this.out;
    }

    @Override
    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(this.params);
    }

    @Override
    public boolean wantsDataRenderedAs(MIMEType type) {
        return this.requestedTypes.resolveSupportedType(type) != null;
    }

    @Override
    public MIMEType resolveRequestedType(MIMEType supported) {
        return this.requestedTypes.resolveSupportedType(supported);
    }

    @Override
    public boolean renderIfRequested(MIMEType supportedType, BiConsumer<MIMEType, DisplayData> renderFunction) {
        MIMEType resolvedType = this.requestedTypes.resolveSupportedType(supportedType);
        if (resolvedType != null) {
            renderFunction.accept(resolvedType, this.getOutputContainer());
            return true;
        }
        return false;
    }

    @Override
    public boolean renderIfRequested(MIMEType supportedType, Function<MIMEType, Object> renderFunction) {
        MIMEType resolvedType = this.requestedTypes.resolveSupportedType(supportedType);
        if (resolvedType != null) {
            this.getOutputContainer().putData(resolvedType, renderFunction.apply(resolvedType));
            return true;
        }
        return false;
    }

    @Override
    public boolean renderIfRequested(MIMEType supportedType, Supplier<Object> render) {
        MIMEType resolvedType = this.requestedTypes.resolveSupportedType(supportedType);
        if (resolvedType != null) {
            this.getOutputContainer().putData(resolvedType, render.get());
            return true;
        }
        return false;
    }
}

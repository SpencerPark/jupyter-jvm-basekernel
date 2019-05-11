package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.display.DisplayDataRenderable;
import io.github.spencerpark.jupyter.api.display.RenderFunction;
import io.github.spencerpark.jupyter.api.display.Renderer;
import io.github.spencerpark.jupyter.api.display.mime.MIMEType;
import io.github.spencerpark.jupyter.kernel.util.InheritanceIterator;

import java.util.*;

/**
 * A default renderer may be set and maps a group to a specific subtype.
 * <p>
 * A suffix may be mapped to a type.
 * <p>
 * A type has a default mime type (may be a list) and is always also rendered
 * as text/plain with toString().
 * <p>
 * A type must also have other supported types.
 * <p>
 * Objects that implement the render interface override their default renders
 * but in the event that renderAs (or displayAs) is invoked the specified types
 * override the defaults.
 */
public class DefaultRenderer implements Renderer {
    private static class RenderFunctionProps {
        private final RenderFunction function;
        private final Set<MIMEType> supportedTypes;
        private final Set<MIMEType> preferredTypes;

        public RenderFunctionProps(RenderFunction function, Set<MIMEType> supportedTypes, Set<MIMEType> preferredTypes) {
            this.function = function;
            this.supportedTypes = supportedTypes;
            this.preferredTypes = preferredTypes;
        }

        public RenderFunction getFunction() {
            return function;
        }

        public Set<MIMEType> getSupportedTypes() {
            return supportedTypes;
        }

        public Set<MIMEType> getPreferredTypes() {
            return preferredTypes;
        }
    }

    private final Map<Class, List<RenderFunctionProps>> renderFunctions;
    private final Map<String, MIMEType> suffixMappings;

    public DefaultRenderer() {
        this.renderFunctions = new HashMap<>();
        this.suffixMappings = new HashMap<>();
    }

    @Override
    public <T> void register(Set<MIMEType> supported, Set<MIMEType> preferred, Set<Class<? extends T>> types, RenderFunction<T> function) {
        RenderFunctionProps props = new RenderFunctionProps(function, supported, preferred);

        types.forEach(c -> this.renderFunctions.compute(c, (k, v) -> {
            List<RenderFunctionProps> functions = v != null ? v : new LinkedList<>();
            functions.add(props);
            return functions;
        }));
    }

    private static DisplayData finalizeDisplayData(DisplayData data, Object value) {
        if (!data.hasDataForType(MIMEType.TEXT_PLAIN))
            data.putText(String.valueOf(value));

        return data;
    }

    /**
     * Render the object with the preferred render type.
     * <p>
     * The rendering algorithm is as follows:
     * <ol>
     * <li>
     * The object is rendered as {@code text/plain} with {@link String#valueOf(Object)}.
     * </li>
     * <li>
     * If the object is {@link DisplayDataRenderable} ask it to render itself as the {@link DisplayDataRenderable#getPreferredRenderTypes() preferred types}.
     * </li>
     * <li>
     * Else iterate over the implemented with the {@link InheritanceIterator} until a render function is found. Use this
     * function to render the object.
     * </li>
     * </ol>
     *
     * @param value  the object to render.
     * @param params a map of parameters that render functions may use.
     *
     * @return the data container holding the rendered view of the {@code value}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public DisplayData render(Object value, Map<String, Object> params) {
        DisplayData out = new DisplayData();

        if (value instanceof DisplayDataRenderable) {
            DisplayDataRenderable renderable = (DisplayDataRenderable) value;

            RenderRequestTypes.Builder requestTypes = new RenderRequestTypes.Builder(this.suffixMappings::get);
            requestTypes.withType(MIMEType.TEXT_PLAIN);
            renderable.getPreferredRenderTypes().forEach(requestTypes::withType);

            renderable.render(new DefaultRenderContext(requestTypes.build(), this, params, out));

            return finalizeDisplayData(out, value);
        }

        Iterator<Class> inheritedTypes = new InheritanceIterator(value.getClass());
        while (inheritedTypes.hasNext()) {
            Class type = inheritedTypes.next();

            List<RenderFunctionProps> allRenderFunctionProps = this.renderFunctions.get(type);
            if (allRenderFunctionProps != null && !allRenderFunctionProps.isEmpty()) {
                for (RenderFunctionProps renderFunctionProps : allRenderFunctionProps) {
                    RenderRequestTypes.Builder requestTypes = new RenderRequestTypes.Builder(this.suffixMappings::get);
                    requestTypes.withType(MIMEType.TEXT_PLAIN);
                    renderFunctionProps.getPreferredTypes().forEach(requestTypes::withType);

                    renderFunctionProps.getFunction().render(
                            value,
                            new DefaultRenderContext(requestTypes.build(), this, params, out)
                    );
                }

                return finalizeDisplayData(out, value);
            }
        }

        return finalizeDisplayData(out, value);
    }

    /**
     * Render the object as the specified types if possible.
     * <p>
     * The rendering algorithm is as follows:
     * <ol>
     * <li>
     * The object is rendered as {@code text/plain} with {@link String#valueOf(Object)} no
     * matter what types are requested.
     * </li>
     * <li>
     * If the object is {@link DisplayDataRenderable} and any of it's {@link DisplayDataRenderable#getSupportedRenderTypes() supported types}
     * are requested, it is asked to render itself.
     * </li>
     * <li>
     * While all of the requested types have not be rendered yet:
     * <ol>
     * <li>
     * For every type in the {@link InheritanceIterator}, apply the same scheme as step 2.
     * </li>
     * <li>
     * Remove all rendered types from the request.
     * </li>
     * </ol>
     * </li>
     * </ol>
     *
     * @param value  the object to render.
     * @param params a map of parameters that render functions may use.
     * @param types  the {@link MIMEType#parse(String) MIME types} to render the object as.
     *
     * @return a {@link DisplayData} container with all the rendered data.
     */
    @SuppressWarnings("unchecked")
    @Override
    public DisplayData renderAs(Object value, Map<String, Object> params, String... types) {
        DisplayData out = new DisplayData();

        RenderRequestTypes.Builder builder = new RenderRequestTypes.Builder(this.suffixMappings::get);
        builder.withType(MIMEType.TEXT_PLAIN);
        for (String type : types)
            builder.withType(type);

        RenderRequestTypes requestTypes = builder.build();
        DefaultRenderContext context = new DefaultRenderContext(requestTypes, this, params, out);

        if (value instanceof DisplayDataRenderable) {
            DisplayDataRenderable renderable = (DisplayDataRenderable) value;
            if (requestTypes.anyRequestedIsSupported(renderable.getSupportedRenderTypes())) {
                renderable.render(context);
                requestTypes.removeFulfilledRequests(out);
            }
        }

        Iterator<Class> inheritedTypes = new InheritanceIterator(value.getClass());
        while (inheritedTypes.hasNext() && !requestTypes.isEmpty()) {
            Class type = inheritedTypes.next();
            List<RenderFunctionProps> allRenderFunctionProps = this.renderFunctions.get(type);
            if (allRenderFunctionProps != null) {
                for (RenderFunctionProps renderFunctionProps : allRenderFunctionProps) {
                    if (requestTypes.anyRequestedIsSupported(renderFunctionProps.getSupportedTypes())) {
                        renderFunctionProps.getFunction().render(value, context);
                        requestTypes.removeFulfilledRequests(out);
                    }
                }
            }
        }

        return finalizeDisplayData(out, value);
    }
}

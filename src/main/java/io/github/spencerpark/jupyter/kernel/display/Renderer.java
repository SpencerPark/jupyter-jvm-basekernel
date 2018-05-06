package io.github.spencerpark.jupyter.kernel.display;

import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;
import io.github.spencerpark.jupyter.kernel.display.mime.MIMETypeParseException;
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
public class Renderer {
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

    public class RenderRegistration<T> {
        private final Set<MIMEType> supported;
        private final Set<MIMEType> preferred;
        private final Set<Class<? extends T>> types;

        public RenderRegistration(Class<T> type) {
            this.supported = new LinkedHashSet<>();
            this.preferred = new LinkedHashSet<>();
            this.types = new LinkedHashSet<>();
            this.types.add(type);
        }

        public RenderRegistration<T> supporting(MIMEType... types) {
            Collections.addAll(this.supported, types);
            return this;
        }

        public RenderRegistration<T> preferring(MIMEType... types) {
            supporting(types);
            Collections.addAll(this.preferred, types);
            return this;
        }

        public RenderRegistration<T> supporting(String... types) {
            for (String type : types)
                this.supported.add(MIMEType.parse(type));
            return this;
        }

        public RenderRegistration<T> preferring(String... types) {
            supporting(types);
            for (String type : types)
                this.preferred.add(MIMEType.parse(type));
            return this;
        }

        public RenderRegistration<T> onType(Class<? extends T> type) {
            this.types.add(type);
            return this;
        }

        public void register(RenderFunction<T> function) {
            Renderer.this.register(supported, preferred, types, function);
        }
    }

    private final Map<Class, RenderFunctionProps> renderFunctions;
    private final Map<String, MIMEType> suffixMappings;

    public Renderer() {
        this.renderFunctions = new HashMap<>();
        this.suffixMappings = new HashMap<>();
    }

    public <T> RenderRegistration<T> createRegistration(Class<T> type) {
        return new RenderRegistration<>(type);
    }

    public <T> void register(Set<MIMEType> supported, Set<MIMEType> preferred, Set<Class<? extends T>> types, RenderFunction<T> function) {

    }

    private DisplayData initializeDisplayData(Object value) {
        return new DisplayData(String.valueOf(value));
    }

    /**
     * Render the data with toString at text/plain.
     * <ol>
     * <li>Render the data with {@link Object#toString()} and store it at {@code text/plain}.</li>
     * <li>
     * If data {@code instanceof} {@link DisplayDataRenderable} then use that
     * to render the data.
     * </li>
     * <li>
     * Try to resolve an external render function for the type by looking for function
     * registrations on:
     * <ol>
     * <li>the type of {@code value}</li>
     * <li>
     * any implemented interface of {@code value} in the left to right order
     * it is declared.
     * </li>
     * <li>
     * for each implemented interface of {@code value} try to use a
     * superinterface of that type. Search the entire hierarchy from left
     * to right.
     * </li>
     * <li>
     * repeat the steps with the supertype
     * </li>
     * </ol>
     * </li>
     * </ol>
     *
     * @param value the object to render.
     * @param params a map of render parameter keys to values.
     *
     * @return the data container holding the rendered view of the {@code value}.
     */
    public DisplayData render(Object value, Map<String, Object> params) {
        DisplayData out = this.initializeDisplayData(value);

        RenderRequestTypes.Builder requestTypes = new RenderRequestTypes.Builder(this.suffixMappings::get);

        if (value instanceof DisplayDataRenderable) {
            DisplayDataRenderable renderable = (DisplayDataRenderable) value;
            renderable.getPreferredRenderTypes().forEach(requestTypes::withType);
            renderable.render(
                    new RenderContext(requestTypes.build(), this, params, out)
            );
            return out;
        }

        Iterator<Class> inheritedTypes = new InheritanceIterator(value.getClass());
        while (inheritedTypes.hasNext()) {
            Class type = value.getClass();
            RenderFunctionProps renderFunctionProps = this.renderFunctions.get(type);
            if (renderFunctionProps != null) {
                renderFunctionProps.getPreferredTypes().forEach(requestTypes::withType);
                renderFunctionProps.getFunction().render(
                        value,
                        new RenderContext(requestTypes.build(), this, params, out)
                );
                return out;
            }
        }

        return out;
    }

    public DisplayData render(Object value) {
        return render(value, new LinkedHashMap<>());
    }

    /**
     * TODO this javadoc no longer makes any sense
     * Render the data with toString at text/plain.
     * <p>
     * For each type:
     * <ol>
     * <li>if just a group then resolve use the default else use first match</li>
     * <li>else if a renderer is defined for the type use it</li>
     * <li>else if the type has a suffix with a delegate resolve that</li>
     * <li>else if supertype supports the renderer use that</li>
     * <li>skip it</li>
     * </ol>
     * <p>
     * image/svg+xml can be triggered via image/svg or application/xml
     * MIMEPattern.parse("image/svg+xml")
     * .matches("image/svg") // true
     * .matches("application/xml") // true
     * <p>
     * renderAs("application/xml") works on a render function that accepts "image/svg+xml"
     * <p>
     * <pre>
     * {@code
     * renderFunc(data, into, as, context) {
     *     // Called with "application/xml"
     *     if (as contains "image/svg+xml") {
     *         // Render as svg into data at "image/svg+xml"?
     *         // We asked for "application/xml"!
     *     }
     * }
     * </pre>
     *
     * @param value
     * @param types
     *
     * @return
     */
    public DisplayData renderAs(Object value, Map<String, Object> params, String... types) {
        DisplayData out = this.initializeDisplayData(value);

        RenderRequestTypes.Builder builder = new RenderRequestTypes.Builder(this.suffixMappings::get);
        for (String type : types)
            builder.withType(type);

        RenderRequestTypes requestTypes = builder.build();
        RenderContext context = new RenderContext(requestTypes, this, params, out);

        if (value instanceof DisplayDataRenderable) {
            DisplayDataRenderable renderable = (DisplayDataRenderable) value;
            if (requestTypes.anyIsRequested(renderable.getSupportedRenderTypes())) {
                renderable.render(context);
                requestTypes.removeFulfilledRequests(out);
            }
        }

        Iterator<Class> inheritedTypes = new InheritanceIterator(value.getClass());
        while (inheritedTypes.hasNext() && !requestTypes.isEmpty()) {
            Class type = value.getClass();
            RenderFunctionProps renderFunctionProps = this.renderFunctions.get(type);
            if (renderFunctionProps != null
                    && requestTypes.anyIsRequested(renderFunctionProps.getSupportedTypes())) {
                renderFunctionProps.getFunction().render(value, context);
                requestTypes.removeFulfilledRequests(out);
            }
        }

        return out;
    }

    public DisplayData renderAs(Object value, String... types) {
        return this.renderAs(value, new LinkedHashMap<>(), types);
    }
}

package io.github.spencerpark.jupyter.api.display;

import io.github.spencerpark.jupyter.api.display.mime.MIMEType;

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
public interface Renderer {
    public static class RenderRegistrationRequest<T> {
        private final Renderer renderer;
        private final Set<MIMEType> supported;
        private final Set<MIMEType> preferred;
        private final Set<Class<? extends T>> types;

        public RenderRegistrationRequest(Renderer renderer, Class<? extends T> type) {
            this.renderer = renderer;
            this.supported = new LinkedHashSet<>();
            this.preferred = new LinkedHashSet<>();
            this.types = new LinkedHashSet<>();
            this.types.add(type);
        }

        public RenderRegistrationRequest<T> supporting(MIMEType... types) {
            Collections.addAll(this.supported, types);
            return this;
        }

        public RenderRegistrationRequest<T> preferring(MIMEType... types) {
            supporting(types);
            Collections.addAll(this.preferred, types);
            return this;
        }

        public RenderRegistrationRequest<T> supporting(String... types) {
            for (String type : types)
                this.supported.add(MIMEType.parse(type));
            return this;
        }

        public RenderRegistrationRequest<T> preferring(String... types) {
            supporting(types);
            for (String type : types)
                this.preferred.add(MIMEType.parse(type));
            return this;
        }

        public RenderRegistrationRequest<T> onType(Class<? extends T> type) {
            this.types.add(type);
            return this;
        }

        public void register(RenderFunction<T> function) {
            Set<MIMEType> supported = this.supported.isEmpty() ? DisplayDataRenderable.ANY : this.supported;
            Set<MIMEType> preferred = this.preferred.isEmpty() ? supported : this.preferred;
            this.renderer.register(supported, preferred, types, function);
        }
    }

    public default <T> RenderRegistrationRequest<T> createRegistration(Class<T> type) {
        return new RenderRegistrationRequest<>(this, type);
    }

    public <T> void register(Set<MIMEType> supported, Set<MIMEType> preferred, Set<Class<? extends T>> types, RenderFunction<T> function);

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
    public DisplayData render(Object value, Map<String, Object> params);

    /**
     * A {@link #render(Object, Map)} variant that supplies an empty parameter map.
     *
     * @param value the object to render.
     *
     * @return a {@link DisplayData} container with all the rendered data.
     */
    public default DisplayData render(Object value) {
        return render(value, new LinkedHashMap<>());
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
    public DisplayData renderAs(Object value, Map<String, Object> params, String... types);

    /**
     * A {@link #renderAs(Object, Map, String...)} variant that supplies an empty parameter map.
     *
     * @param value the object to render.
     * @param types the {@link MIMEType#parse(String) MIME types} to render the object as.
     *
     * @return a {@link DisplayData} container with all the rendered data.
     */
    public default DisplayData renderAs(Object value, String... types) {
        return this.renderAs(value, new LinkedHashMap<>(), types);
    }
}

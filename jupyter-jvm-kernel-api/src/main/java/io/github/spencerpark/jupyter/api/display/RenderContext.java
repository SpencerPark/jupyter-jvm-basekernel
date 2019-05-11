package io.github.spencerpark.jupyter.api.display;

import io.github.spencerpark.jupyter.api.display.mime.MIMEType;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RenderContext {

    public Renderer getRenderer();

    public DisplayData getOutputContainer();

    /**
     * @return the immutable parameter map. Prefer the specialized get parameter methods.
     */
    public Map<String, Object> getParams();

    public default Object getParameter(String key) {
        return this.getParams().get(key);
    }

    public default Object getParameter(String key, Object defaultValue) {
        return this.getParams().getOrDefault(key, defaultValue);
    }

    public default String getParameterAsString(String key) {
        Object value = this.getParameter(key);
        return value == null ? null : String.valueOf(value);
    }

    public default String getParameterAsString(String key, String defaultValue) {
        String value = this.getParameterAsString(key);
        return value == null ? defaultValue : value;
    }

    public default Integer getParameterAsInt(String key) {
        Object value = this.getParameter(key);
        return value == null
                ? null
                : value instanceof Number
                        ? ((Number) value).intValue()
                        : Integer.parseInt(String.valueOf(value));
    }

    public default Integer getParameterAsInt(String key, Integer defaultValue) {
        Integer value = this.getParameterAsInt(key);
        return value == null ? defaultValue : value;
    }

    public default Double getParameterAsDouble(String key) {
        Object value = this.getParameter(key);
        return value == null
                ? null
                : value instanceof Number
                        ? ((Number) value).doubleValue()
                        : Double.parseDouble(String.valueOf(value));
    }

    public default Double getParameterAsDouble(String key, Double defaultValue) {
        Double value = this.getParameterAsDouble(key);
        return value == null ? defaultValue : value;
    }

    public default Boolean getParameterAsBoolean(String key) {
        Object value = this.getParameter(key);
        return value == null
                ? null
                : value instanceof Boolean
                        ? (Boolean) value
                        : Boolean.parseBoolean(String.valueOf(value));
    }

    public default Boolean getParameterAsBoolean(String key, Boolean defaultValue) {
        Boolean value = this.getParameterAsBoolean(key);
        return value == null ? defaultValue : value;
    }

    public boolean wantsDataRenderedAs(MIMEType type);

    public MIMEType resolveRequestedType(MIMEType supported);

    public boolean renderIfRequested(MIMEType supportedType, BiConsumer<MIMEType, DisplayData> renderFunction);

    public boolean renderIfRequested(MIMEType supportedType, Function<MIMEType, Object> renderFunction);

    public boolean renderIfRequested(MIMEType supportedType, Supplier<Object> render);
}

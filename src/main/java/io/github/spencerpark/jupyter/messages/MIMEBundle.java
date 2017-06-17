package io.github.spencerpark.jupyter.messages;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MIMEBundle implements Map<String, Object> {
    public static final MIMEBundle EMPTY = new MIMEBundle(Collections.emptyMap());

    public static final MIMEBundle EMPTY_STRING = new MIMEBundle("");

    public static MIMEBundle emptyIfNull(MIMEBundle bundle) {
        return bundle == null ? EMPTY : bundle;
    }

    private final Map<String, Object> data;

    public MIMEBundle(Map<String, Object> data) {
        this.data = data;
    }

    public MIMEBundle() {
        this.data = new LinkedHashMap<>();
    }

    public MIMEBundle(String text) {
        this.data = new LinkedHashMap<>();
        this.putText(text);
    }

    public void putText(String text) {
        this.data.put("text/plain", text);
    }

    public void putHTML(String html) {
        this.data.put("text/html", html);
    }

    //TODO add some typesafe setters for the commonly supported MIME types
    //https://github.com/ipython/ipython/blob/master/IPython/core/display.py
    public Map<String, Object> getData() {
        return data;
    }

    /*
     * Delegate method implementations
     */
    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return data.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<Object> values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return data.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return data.equals(o);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        data.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        data.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return data.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return data.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return data.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return data.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return data.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return data.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return data.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return data.merge(key, value, remappingFunction);
    }
}

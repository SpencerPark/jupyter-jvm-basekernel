package io.github.spencerpark.jupyter.ipywidgets.props;

import java.util.List;

public interface MultiRawDataWidgetProperty<V> extends WidgetProperty<V> {
    public List<byte[]> toBytes(V obj);

    public V fromBytes(List<byte[]> data);
}

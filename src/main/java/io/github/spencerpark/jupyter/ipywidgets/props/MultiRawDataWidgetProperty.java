package io.github.spencerpark.jupyter.ipywidgets.props;

import java.util.List;

public interface MultiRawDataWidgetProperty<V> extends WidgetProperty<V> {
    public List<byte[]> toBytes();

    public void fromBytes(List<byte[]> data);
}

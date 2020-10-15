package io.github.spencerpark.jupyter.ipywidgets.props;

public interface RawDataWidgetProperty<V> extends WidgetProperty<V> {
    public byte[] toBytes(V data);

    public V fromBytes(byte[] data);
}

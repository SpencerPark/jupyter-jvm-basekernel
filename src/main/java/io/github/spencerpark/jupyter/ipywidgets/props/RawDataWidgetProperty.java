package io.github.spencerpark.jupyter.ipywidgets.props;

public interface RawDataWidgetProperty<V> extends WidgetProperty<V> {
    public byte[] toBytes();

    public void fromBytes(byte[] data);
}

package io.github.spencerpark.jupyter.kernel.magic;

public interface CellMagicArgs extends LineMagicArgs {
    public String getBody();

    public String getRawCell();
}

package io.github.spencerpark.jupyter.kernel.magic;

import java.util.List;

public interface LineMagicArgs {
    public String getRaw();

    public String getName();

    public List<String> getArgs();
}

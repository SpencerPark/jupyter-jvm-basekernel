package io.github.spencerpark.jupyter.api.magic;

import io.github.spencerpark.jupyter.api.magic.registry.CellMagic;
import io.github.spencerpark.jupyter.api.magic.registry.LineMagic;

import java.util.List;

public class StaticMagics {
    @LineMagic
    public static int staticMagic(List<String> args) {
        return args.size();
    }

    @CellMagic("staticMagic")
    public static String staticCellMagic(List<String> args, String body) {
        return body;
    }
}

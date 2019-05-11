package io.github.spencerpark.jupyter.api.magic.registry;

import java.util.List;

public interface Magics {
    public <T> T applyLineMagic(String name, List<String> args) throws Exception;

    public <T> T applyCellMagic(String name, List<String> args, String body) throws Exception;

    // Magic registration

    public void registerLineMagic(String name, LineMagicFunction<?> magic);

    public void registerCellMagic(String name, CellMagicFunction<?> magic);

    public default  <T extends LineMagicFunction<?>&CellMagicFunction<?>> void registerLineCellMagic(String name, T magic) {
        this.registerLineMagic(name, magic);
        this.registerCellMagic(name, magic);
    }

    // Reflective magic registration

    public void registerMagics(Object magics);

    public void registerMagics(Class<?> magicsClass);
}

package io.github.spencerpark.jupyter.api.magic;

public interface CellMagicParseContext {
    public static CellMagicParseContext of(CellMagicArgs args, String rawArgsLine, String rawCell) {
        return new CellMagicParseContext() {
            @Override
            public CellMagicArgs getMagicCall() {
                return args;
            }

            @Override
            public String getRawArgsLine() {
                return rawArgsLine;
            }

            @Override
            public String getRawCell() {
                return rawCell;
            }
        };
    }

    public CellMagicArgs getMagicCall();

    public String getRawArgsLine();

    public String getRawCell();
}

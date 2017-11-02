package io.github.spencerpark.jupyter.kernel.magic;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class MagicParserTest {
    private MagicParser parser;

    @Before
    public void setUp() throws Exception {
        this.parser = new MagicParser("//%", "//%%");
    }

    @Test
    public void transformLineMagics() {
        String cell = Stream.of(
                "//%magicName arg1 arg2",
                "Inline magic = //%magicName2 arg1",
                "//Just a comment",
                "//%magicName3 arg1 \"arg2 arg2\""
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.parser.transformLineMagics(cell, args ->
            "**" + args.getName() + "-" + args.getArgs().stream().collect(Collectors.joining(","))
        );

        String expectedTransformedCell = Stream.of(
                "**magicName-arg1,arg2",
                "Inline magic = **magicName2-arg1",
                "//Just a comment",
                "**magicName3-arg1,arg2 arg2"
        ).collect(Collectors.joining("\n"));

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void parseCellMagic() {
        String cell = Stream.of(
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        CellMagicArgs args = this.parser.parseCellMagic(cell);

        assertEquals("cellMagicName", args.getName());
        assertEquals(Arrays.asList("arg1", "arg2 arg2", "arg3"), args.getArgs());
        assertEquals("This is the body", args.getBody());
        assertEquals("//%%cellMagicName arg1 \"arg2 arg2\" arg3  ", args.getRaw());
        assertEquals(cell, args.getRawCell());
    }

    @Test
    public void transformCellMagic() {
        String cell = Stream.of(
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.parser.transformCellMagic(cell, args ->
            args.getName() + "(" + args.getArgs().stream().collect(Collectors.joining(",")) + ")" + "\n"
                + args.getBody()
        );

        String expectedTransformedCell = "cellMagicName(arg1,arg2 arg2,arg3)\nThis is the body";

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void dontTransformNonMagicCell() {
        String cell = Stream.of(
                "//%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body"
        ).collect(Collectors.joining("\n"));

        String transformedCell = this.parser.transformCellMagic(cell, args -> "transformer applied");

        assertEquals(cell, transformedCell);
    }
}
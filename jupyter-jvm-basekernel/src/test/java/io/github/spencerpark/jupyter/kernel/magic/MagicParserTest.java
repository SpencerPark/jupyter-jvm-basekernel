package io.github.spencerpark.jupyter.kernel.magic;

import io.github.spencerpark.jupyter.api.magic.CellMagicParseContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MagicParserTest {
    @SafeVarargs
    @SuppressWarnings("varargs")
    private static <T> List<T> list(T... args) {
        return args.length == 1 ? Collections.singletonList(args[0]) : Arrays.asList(args);
    }

    private MagicParser inlineParser;
    private MagicParser solParser;

    @Before
    public void setUp() throws Exception {
        this.inlineParser = new MagicParser("//%", "//%%");
        this.solParser = new MagicParser("^\\s*//%", "//%%");
    }

    @Test
    public void transformLineMagics() {
        String cell = String.join("\n",
                "//%magicName arg1 arg2",
                "Inline magic = //%magicName2 arg1",
                "//Just a comment",
                "//%magicName3 arg1 \"arg2 arg2\""
        );

        String transformedCell = this.inlineParser.transformLineMagics(cell, ctx ->
                "**" + ctx.getMagicCall().getName() + "-" + String.join(",", ctx.getMagicCall().getArgs())
        );

        String expectedTransformedCell = String.join("\n",
                "**magicName-arg1,arg2",
                "Inline magic = **magicName2-arg1",
                "//Just a comment",
                "**magicName3-arg1,arg2 arg2"
        );

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void parseCellMagic() {
        String cell = String.join("\n",
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body",
                "with multiple lines"
        );

        CellMagicParseContext ctx = this.inlineParser.parseCellMagic(cell);

        assertNotNull(ctx);
        assertEquals("cellMagicName", ctx.getMagicCall().getName());
        assertEquals(list("arg1", "arg2 arg2", "arg3"), ctx.getMagicCall().getArgs());
        assertEquals("This is the body\nwith multiple lines", ctx.getMagicCall().getBody());
        assertEquals("//%%cellMagicName arg1 \"arg2 arg2\" arg3  ", ctx.getRawArgsLine());
        assertEquals(cell, ctx.getRawCell());
    }

    @Test
    public void split() {
        String origCell = String.join("\n",
                "//%magic arg\" with \"embedded\" quotes\"",
                "//%magic arg1 \"\" \" \" arg2",
                "//%magic escaped\\ space arg2",
                "//%magic unclosed \"arg 2",
                "//%magic \\e\\s\\c\\a\\p\\e\\d with \\\\",
                "//%magic arg1 --keyword=\"arg 2\" ",
                "//%magic   trailing\t   spaces   ",
                "//%magic \\\"escaped quotes\\\""
        );
        List<List<String>> transformedLines = new LinkedList<>();
        this.inlineParser.transformLineMagics(origCell, ctx -> {
            transformedLines.add(ctx.getMagicCall().getArgs());
            return "transformed";
        });

        assertEquals(list(
                list("arg with embedded quotes"),
                list("arg1", "", " ", "arg2"),
                list("escaped space", "arg2"),
                list("unclosed", "arg 2"),
                list("escaped", "with", "\\"),
                list("arg1", "--keyword=arg 2"),
                list("trailing", "spaces"),
                list("\"escaped", "quotes\"")
        ), transformedLines);
    }

    @Test
    public void transformCellMagic() {
        String cell = String.join("\n",
                "//%%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body",
                "with multiple lines"
        );

        String transformedCell = this.inlineParser.transformCellMagic(cell, ctx ->
                ctx.getMagicCall().getName() + "(" + String.join(",", ctx.getMagicCall().getArgs()) + ")" + "\n"
                        + ctx.getMagicCall().getBody()
        );

        String expectedTransformedCell = "cellMagicName(arg1,arg2 arg2,arg3)\nThis is the body\nwith multiple lines";

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void dontTransformNonMagicCell() {
        String cell = String.join("\n",
                "//%cellMagicName arg1 \"arg2 arg2\" arg3  ",
                "This is the body",
                "with multiple lines"
        );

        String transformedCell = this.inlineParser.transformCellMagic(cell, ctx -> "transformer applied");

        assertEquals(cell, transformedCell);
    }

    @Test
    public void startOfLineParserSkipsInlineMagics() {
        String cell = "System.out.printf(\"Fmt //%s string\", \"test\");";

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> "");

        assertEquals(cell, transformedCell);
    }

    @Test
    public void startOfLineParserAllowsWhitespace() {
        String cell = String.join("\n",
                "//%sol",
                "  //%sol2",
                "\t//%sol3"
        );

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> ctx.getMagicCall().getName());
        String expectedTransformedCell = String.join("\n",
                "sol",
                "sol2",
                "sol3"
        );

        assertEquals(expectedTransformedCell, transformedCell);
    }

    @Test
    public void startOfLineParserSkipsInline() {
        String cell = String.join("\n",
                "//%sol",
                "Not //%sol"
        );

        String transformedCell = this.solParser.transformLineMagics(cell, ctx -> ctx.getMagicCall().getName());
        String expectedTransformedCell = String.join("\n",
                "sol",
                "Not //%sol"
        );

        assertEquals(expectedTransformedCell, transformedCell);
    }
}
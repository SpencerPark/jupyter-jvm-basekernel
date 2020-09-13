package io.github.spencerpark.jupyter.kernel.magic;

import io.github.spencerpark.jupyter.api.magic.CellMagicArgs;
import io.github.spencerpark.jupyter.api.magic.CellMagicParseContext;
import io.github.spencerpark.jupyter.api.magic.LineMagicArgs;
import io.github.spencerpark.jupyter.api.magic.LineMagicParseContext;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MagicParser {
    public static List<String> split(String args) {
        List<String> split = new LinkedList<>();

        StringBuilder current = new StringBuilder();
        boolean inArg = false;
        boolean inQuotes = false;
        boolean escape = false;
        for (char c : args.toCharArray()) {
            if (escape) {
                // No matter what the character is, it is meant literally.
                current.append(c);
                escape = false;
                continue;
            }

            switch (c) {
                case ' ':
                case '\t':
                    if (inQuotes) {
                        // In quotes so this whitespace is part of the arg.
                        current.append(c);
                    } else if (inArg) {
                        // This whitespace must be closing the arg. If current.length() == 0 then it
                        // is the case of extra space between args. In that case we don't want to include
                        // an empty arg in the middle...
                        split.add(current.toString());
                        current.setLength(0);
                        inArg = false;
                    }
                    break;
                case '\\':
                    escape = true;
                    inArg = true;
                    break;
                case '"':
                    // An opening or closing quote that is not escaped. The quote is not
                    // part of the arg.
                    if (inQuotes) {
                        inQuotes = false;
                    } else {
                        inArg = true;
                        inQuotes = true;
                    }
                    break;
                default:
                    current.append(c);
                    inArg = true;
                    break;
            }
        }

        // The non-empty tail of the string is the last arg.
        if (current.length() > 0) {
            split.add(current.toString());
        }

        return split;
    }

    private final Pattern lineMagicPattern;
    private final Pattern cellMagicPattern;

    public MagicParser() {
        this("^%", "%%");
    }

    public MagicParser(String lineMagicStart, String cellMagicStart) {
        this.lineMagicPattern = Pattern.compile(lineMagicStart + "(?<args>\\w.*?)$", Pattern.MULTILINE);
        this.cellMagicPattern = Pattern.compile("^(?<argsLine>" + cellMagicStart + "(?<args>\\w.*?))\\R(?<body>(?sU).+?)$");
    }

    public String transformLineMagics(String cell, Function<LineMagicParseContext, String> transformer) {
        StringBuffer transformedCell = new StringBuffer();

        Matcher m = this.lineMagicPattern.matcher(cell);
        while (m.find()) {
            String raw = m.group();
            String rawArgs = m.group("args");
            List<String> split = split(rawArgs);

            LineMagicArgs args = LineMagicArgs.of(split.get(0), split.subList(1, split.size()));
            LineMagicParseContext ctx = LineMagicParseContext.of(args, raw, cell, cell.substring(0, m.start()));

            String transformed = transformer.apply(ctx);
            if (transformed == null) transformed = raw;

            m.appendReplacement(transformedCell, Matcher.quoteReplacement(transformed));
        }
        m.appendTail(transformedCell);

        return transformedCell.toString();
    }

    public CellMagicParseContext parseCellMagic(String cell) {
        Matcher m = this.cellMagicPattern.matcher(cell);

        if (!m.matches()) return null;

        String rawArgsLine = m.group("argsLine");
        String rawArgs = m.group("args");
        String body = m.group("body");
        List<String> split = split(rawArgs);

        CellMagicArgs args = CellMagicArgs.of(split.get(0), split.subList(1, split.size()), body);
        return CellMagicParseContext.of(args, rawArgsLine, cell);
    }

    public String transformCellMagic(String cell, Function<CellMagicParseContext, String> transformer) {
        CellMagicParseContext ctx = this.parseCellMagic(cell);

        return ctx == null ? cell : transformer.apply(ctx);
    }
}

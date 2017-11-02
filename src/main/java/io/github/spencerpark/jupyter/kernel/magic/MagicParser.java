package io.github.spencerpark.jupyter.kernel.magic;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MagicParser {
    protected class LineMagicArgsImpl implements LineMagicArgs {
        private final String raw;
        private final String name;
        private final List<String> args;

        public LineMagicArgsImpl(String raw, String name, List<String> args) {
            this.raw = raw;
            this.name = name;
            this.args = args;
        }

        @Override
        public String getRaw() {
            return raw;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> getArgs() {
            return args;
        }
    }

    protected class CellMagicArgsImpl extends LineMagicArgsImpl implements CellMagicArgs {
        private final String rawCell;
        private final String body;

        public CellMagicArgsImpl(String raw, String name, List<String> args, String rawCell, String body) {
            super(raw, name, args);
            this.rawCell = rawCell;
            this.body = body;
        }

        @Override
        public String getBody() {
            return body;
        }

        @Override
        public String getRawCell() {
            return rawCell;
        }
    }

    protected static List<String> split(String args) {
        args = args.trim();

        List<String> split = new LinkedList<>();

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean escape = false;
        for (char c : args.toCharArray()) {
            switch (c) {
                case ' ':
                case '\t':
                    if (inQuotes) {
                        current.append(c);
                    } else if (current.length() > 0) {
                        // If whitespace is closing the string the add the current and reset
                        split.add(current.toString());
                        current.setLength(0);
                    }
                    break;
                case '\\':
                    if (escape) {
                        current.append("\\\\");
                        escape = false;
                    } else {
                        escape = true;
                    }
                    break;
                case '\"':
                    if (escape) {
                        current.append('"');
                        escape = false;
                    } else {
                        if (current.length() > 0 && inQuotes) {
                            split.add(current.toString());
                            current.setLength(0);
                            inQuotes = false;
                        } else {
                            inQuotes = true;
                        }
                    }
                    break;
                default:
                    current.append(c);
            }
        }

        if (current.length() > 0) {
            split.add(current.toString());
        }

        return split;
    }

    private final Pattern lineMagicPattern;
    private final Pattern cellMagicPattern;

    public MagicParser() {
        this("%", "%%");
    }

    public MagicParser(String lineMagicStart, String cellMagicStart) {
        this.lineMagicPattern = Pattern.compile(lineMagicStart + "(?<args>\\w.*?)$", Pattern.MULTILINE);
        this.cellMagicPattern = Pattern.compile("^(?<argsLine>" + cellMagicStart + "(?<args>\\w.*?))\\R(?<body>.+?)$");
    }

    public String transformLineMagics(String cell, Function<LineMagicArgs, String> transformer) {
        StringBuffer transformedCell = new StringBuffer();

        Matcher m = this.lineMagicPattern.matcher(cell);
        while (m.find()) {
            String raw = m.group();
            String rawArgs = m.group("args");
            List<String> split = split(rawArgs);
            LineMagicArgs args = new LineMagicArgsImpl(raw, split.get(0), split.subList(1, split.size()));

            m.appendReplacement(transformedCell, transformer.apply(args));
        }
        m.appendTail(transformedCell);

        return transformedCell.toString();
    }

    public CellMagicArgs parseCellMagic(String cell) {
        Matcher m = this.cellMagicPattern.matcher(cell);

        if (!m.matches()) return null;

        String rawArgsLine = m.group("argsLine");
        String rawArgs = m.group("args");
        String body = m.group("body");
        List<String> split = split(rawArgs);

        return new CellMagicArgsImpl(rawArgsLine, split.get(0), split.subList(1, split.size()), cell, body);
    }

    public String transformCellMagic(String cell, Function<CellMagicArgs, String> transformer) {
        CellMagicArgs args = this.parseCellMagic(cell);

        return args == null ? cell : transformer.apply(args);
    }
}

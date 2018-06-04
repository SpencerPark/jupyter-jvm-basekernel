package io.github.spencerpark.jupyter.kernel.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobFinder {
    private static class GlobSegment implements DirectoryStream.Filter<Path> {
        public static final GlobSegment ANY_FILE = new GlobSegment(Pattern.compile("^.*$"), true);
        public static final GlobSegment ANY_DIR = new GlobSegment(Pattern.compile("^.*$"), false);

        private final String literal;
        private final Pattern regex;

        private final boolean isLast;

        public GlobSegment(String literal, boolean isLast) {
            this.literal = literal;
            this.regex = null;
            this.isLast = isLast;
        }

        public GlobSegment(Pattern regex, boolean isLast) {
            this.literal = null;
            this.regex = regex;
            this.isLast = isLast;
        }

        public boolean isLiteral() {
            return this.literal != null;
        }

        @Override
        public boolean accept(Path s) throws IOException {
            BasicFileAttributes attributes = Files.readAttributes(s, BasicFileAttributes.class);

            if ((attributes.isRegularFile() && !this.isLast) || (attributes.isDirectory() && this.isLast))
                return false;

            Path pathName = s.getFileName();

            if (pathName == null)
                return false;

            String name = pathName.toString();
            return this.literal != null
                    ? this.literal.equals(name)
                    : this.regex.matcher(name).matches();
        }

        @Override
        public String toString() {
            return (isLast ? "file: " : "dir: ") + (isLiteral() ? this.literal : this.regex.pattern());
        }
    }

    private static final Pattern GLOB_SEGMENT_COMPONENT = Pattern.compile(
            "" +
                    "(?<literal>[^*?]+)" +
                    "|(?<wildcard>\\*)" +
                    "|(?<singleWildcard>\\?)" +
                    "|(?:\\\\(?<escaped>[*?]))"
    );

    private static final Pattern SPLITTER = Pattern.compile("/+");

    private final Path base;
    private final List<GlobSegment> segments;

    public GlobFinder(FileSystem fs, String glob) {
        // Split with "/" but match with the actual separator
        String[] segments = SPLITTER.split(glob);
        // If the glob ends with "/" then we add an extra wildcard pattern
        List<GlobSegment> matchers = new ArrayList<>(segments.length + 1);
        int lastBaseSegmentIdx = 0;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            StringBuilder pattern = new StringBuilder();
            StringBuilder lit = new StringBuilder();
            int wildcards = 0;
            int singleWildcards = 0;

            Matcher m = GLOB_SEGMENT_COMPONENT.matcher(segment);
            while (m.find()) {
                String literal = m.group("literal");
                if (literal == null) literal = m.group("escaped");
                if (literal != null) {
                    pattern.append(Pattern.quote(literal));
                    lit.append(literal);
                    continue;
                }

                String wildcard = m.group("wildcard");
                if (wildcard != null) {
                    pattern.append(".*");
                    wildcards++;
                    continue;
                }

                String singleWildcard = m.group("singleWildcard");
                // There are only 4 groups, 3 of which have been checked and are null so this
                // on must be non-null.
                assert singleWildcard != null : "Glob construction pattern incomplete.";
                pattern.append(".");
                singleWildcards++;
            }

            assert m.hitEnd() : "Glob construction missed some characters.";

            if (wildcards == 0 && singleWildcards == 0) {
                matchers.add(new GlobSegment(lit.toString(), i == segments.length - 1));
                if (lastBaseSegmentIdx == i) lastBaseSegmentIdx++;
            } else {
                matchers.add(new GlobSegment(Pattern.compile("^" + pattern.toString() + "$"), i == segments.length - 1));
            }
        }

        if (glob.endsWith("/"))
            matchers.add(GlobSegment.ANY_FILE);

        // Cannot use the very nice `new File(glob).isAbsolute()` solution as this is restricted to the default file
        // system and doesn't use the `fs`. Additionally `Paths.get(glob).isAbsolute()` will fail with an illegal path
        // exception when trying to parse a windows path with a * in it for example. Therefor we need a clean segment.
        boolean isAbsolute = lastBaseSegmentIdx > 0 && fs.getPath(segments[0] + fs.getSeparator()).isAbsolute();
        String firstSeg = isAbsolute ? segments[0] + fs.getSeparator() : "." + fs.getSeparator();

        this.base = fs.getPath(firstSeg, Arrays.copyOfRange(segments, isAbsolute ? 1 : 0, lastBaseSegmentIdx));
        this.segments = matchers.subList(lastBaseSegmentIdx, matchers.size());
    }

    public GlobFinder(String glob) {
        this(FileSystems.getDefault(), glob);
    }

    private void collect(Path dir, GlobSegment segment, List<GlobSegment> segments, Collection<Path> into) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, segment)) {
            boolean isMoreSegments = !segments.isEmpty();
            GlobSegment head = isMoreSegments ? segments.get(0) : null;
            List<GlobSegment> tail = isMoreSegments ? segments.subList(1, segments.size()) : Collections.emptyList();

            for (Path p : files) {
                if (isMoreSegments && Files.isDirectory(p))
                    collect(p, head, tail, into);
                else
                    into.add(p);
            }
        }
    }

    public Iterable<Path> computeMatchingPaths() throws IOException {
        if (this.segments.isEmpty()) {
            if (Files.isDirectory(this.base))
                return Files.newDirectoryStream(this.base, Files::isRegularFile);
            if (Files.isRegularFile(this.base))
                return Collections.singleton(this.base);
            return Collections.emptyList();
        }

        List<Path> paths = new ArrayList<>();
        GlobSegment head = this.segments.get(0);
        List<GlobSegment> tail = this.segments.subList(1, this.segments.size());

        collect(this.base, head, tail, paths);

        return paths;
    }
}


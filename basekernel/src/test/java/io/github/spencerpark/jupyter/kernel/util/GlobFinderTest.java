package io.github.spencerpark.jupyter.kernel.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobFinderTest {
    private static final Configuration WIN_FS = Configuration.windows().toBuilder().setWorkingDirectory("C:/dir-a").build();
    private static final Configuration UNIX_FS = Configuration.unix().toBuilder().setWorkingDirectory("/dir-a").build();
    private static final Configuration OSX_FS = Configuration.osX().toBuilder().setWorkingDirectory("/dir-a").build();

    private static final Set<String> TEST_FILES_ALL = setOf("a.txt", "b.txt", "c.txt", "a.pdf", "b.c.pdf", "abc.svg");
    private static final Set<String> TEST_DIRS_ALL = setOf("dir-a", "dir-b", "dir.c");

    private static Set<String> allFilesMapped(Function<String, String> mapper) {
        return TEST_FILES_ALL
                .stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    private static Set<String> allFilesFilterMapped(Predicate<String> filter, Function<String, String> mapper) {
        return TEST_FILES_ALL
                .stream()
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    private static Set<String> allDirsMapped(Function<String, String> mapper) {
        return TEST_DIRS_ALL
                .stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    private static Set<String> allDirsFlatMapped(Function<String, Set<String>> mapper) {
        return TEST_DIRS_ALL
                .stream()
                .map(mapper)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private static Set<String> allFilesAndDirsMapped(Function<String, String> mapper) {
        return Stream.concat(TEST_FILES_ALL.stream(), TEST_DIRS_ALL.stream())
                .map(mapper)
                .collect(Collectors.toSet());
    }

    private static Set<String> allFilesAndDirsFilterMapped(Predicate<String> filter, Function<String, String> mapper) {
        return Stream.concat(TEST_FILES_ALL.stream(), TEST_DIRS_ALL.stream())
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    private static Set<String> setOf(String... files) {
        return Arrays.stream(files).collect(Collectors.toSet());
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(WIN_FS, "C:/*", allFilesMapped(s -> "C:/" + s), allFilesAndDirsMapped(s -> "C:/" + s)),
                Arguments.of(UNIX_FS, "/*", allFilesMapped(s -> "/" + s), allFilesAndDirsMapped(s -> "/" + s)),
                Arguments.of(OSX_FS, "/*", allFilesMapped(s -> "/" + s), allFilesAndDirsMapped(s -> "/" + s)),

                // Implicit * appended with trailing / in file mode but not path mode.
                Arguments.of(WIN_FS, "C:/*/", allDirsFlatMapped(d -> allFilesMapped(s -> "C:/" + d + "/" + s)), allFilesAndDirsMapped(s -> "C:/" + s)),
                Arguments.of(UNIX_FS, "/*/", allDirsFlatMapped(d -> allFilesMapped(s -> "/" + d + "/" + s)), allFilesAndDirsMapped(s -> "/" + s)),
                Arguments.of(OSX_FS, "/*/", allDirsFlatMapped(d -> allFilesMapped(s -> "/" + d + "/" + s)), allFilesAndDirsMapped(s -> "/" + s)),

                Arguments.of(WIN_FS, "C:/c.txt", setOf("C:/c.txt"), setOf("C:/c.txt")),
                Arguments.of(UNIX_FS, "/c.txt", setOf("/c.txt"), setOf("/c.txt")),
                Arguments.of(OSX_FS, "/c.txt", setOf("/c.txt"), setOf("/c.txt")),

                Arguments.of(WIN_FS, "C:/*/c.txt", allDirsMapped(d -> "C:/" + d + "/c.txt"), allDirsMapped(d -> "C:/" + d + "/c.txt")),
                Arguments.of(UNIX_FS, "/*/c.txt", allDirsMapped(d -> "/" + d + "/c.txt"), allDirsMapped(d -> "/" + d + "/c.txt")),
                Arguments.of(OSX_FS, "/*/c.txt", allDirsMapped(d -> "/" + d + "/c.txt"), allDirsMapped(d -> "/" + d + "/c.txt")),

                Arguments.of(WIN_FS, "C:/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "C:/dir-b/" + f), allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "C:/dir-b/" + f)),
                Arguments.of(UNIX_FS, "/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f), allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f)),
                Arguments.of(OSX_FS, "/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f), allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f)),

                Arguments.of(WIN_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f), allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f)),
                Arguments.of(UNIX_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f), allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f)),
                Arguments.of(OSX_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f), allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f)),

                Arguments.of(WIN_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)), allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f))),
                Arguments.of(UNIX_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)), allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f))),
                Arguments.of(OSX_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)), allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f))),

                Arguments.of(WIN_FS, "?.pdf", setOf("C:/dir-a/a.pdf"), setOf("C:/dir-a/a.pdf")),
                Arguments.of(UNIX_FS, "?.pdf", setOf("/dir-a/a.pdf"), setOf("/dir-a/a.pdf")),
                Arguments.of(OSX_FS, "?.pdf", setOf("/dir-a/a.pdf"), setOf("/dir-a/a.pdf")),

                Arguments.of(WIN_FS, "C:/dir-?/?.pdf", setOf("C:/dir-a/a.pdf", "C:/dir-b/a.pdf"), setOf("C:/dir-a/a.pdf", "C:/dir-b/a.pdf")),
                Arguments.of(UNIX_FS, "/dir-?/?.pdf", setOf("/dir-a/a.pdf", "/dir-b/a.pdf"), setOf("/dir-a/a.pdf", "/dir-b/a.pdf")),
                Arguments.of(OSX_FS, "/dir-?/?.pdf", setOf("/dir-a/a.pdf", "/dir-b/a.pdf"), setOf("/dir-a/a.pdf", "/dir-b/a.pdf")),

                Arguments.of(WIN_FS, "C:/dir.c/abc.svg", setOf("C:/dir.c/abc.svg"), setOf("C:/dir.c/abc.svg")),
                Arguments.of(UNIX_FS, "/dir.c/abc.svg", setOf("/dir.c/abc.svg"), setOf("/dir.c/abc.svg")),
                Arguments.of(OSX_FS, "/dir.c/abc.svg", setOf("/dir.c/abc.svg"), setOf("/dir.c/abc.svg")),

                Arguments.of(WIN_FS, "C:/bad", Collections.emptySet(), Collections.emptySet()),
                Arguments.of(UNIX_FS, "/bad", Collections.emptySet(), Collections.emptySet()),
                Arguments.of(OSX_FS, "/bad", Collections.emptySet(), Collections.emptySet()),

                Arguments.of(WIN_FS, "C:/*/*/*/*/*/*/*", Collections.emptySet(), Collections.emptySet()),
                Arguments.of(UNIX_FS, "/*/*/*/*/*/*/*", Collections.emptySet(), Collections.emptySet()),
                Arguments.of(OSX_FS, "/*/*/*/*/*/*/*", Collections.emptySet(), Collections.emptySet()),

                Arguments.of(WIN_FS, "C:/dir-?/", allDirsFlatMapped(d -> !d.startsWith("dir-") ? Collections.emptySet() : allFilesMapped(f -> "C:/" + d + "/" + f)), setOf("C:/dir-a", "C:/dir-b")),
                Arguments.of(UNIX_FS, "/dir-?/", allDirsFlatMapped(d -> !d.startsWith("dir-") ? Collections.emptySet() : allFilesMapped(f -> "/" + d + "/" + f)), setOf("/dir-a", "/dir-b")),
                Arguments.of(OSX_FS, "/dir-?/", allDirsFlatMapped(d -> !d.startsWith("dir-") ? Collections.emptySet() : allFilesMapped(f -> "/" + d + "/" + f)), setOf("/dir-a", "/dir-b")),

                Arguments.of(WIN_FS, "C:/*c*", allFilesFilterMapped(f -> f.contains("c"), f -> "C:/" + f), allFilesAndDirsFilterMapped(f -> f.contains("c"), f -> "C:/" + f)),
                Arguments.of(UNIX_FS, "/*c*", allFilesFilterMapped(f -> f.contains("c"), f -> "/" + f), allFilesAndDirsFilterMapped(f -> f.contains("c"), f -> "/" + f)),
                Arguments.of(OSX_FS, "/*c*", allFilesFilterMapped(f -> f.contains("c"), f -> "/" + f), allFilesAndDirsFilterMapped(f -> f.contains("c"), f -> "/" + f))
        );
    }

    private static FileSystem setUp(Configuration fsConfig) throws Exception {
        FileSystem fs = Jimfs.newFileSystem(fsConfig);

        List<Path> roots = StreamSupport.stream(fs.getRootDirectories().spliterator(), false).collect(Collectors.toList());
        roots.add(fs.getPath("."));

        for (Path dir1 : roots) {
            for (String dir2 : new String[]{".", "dir-a", "dir-b", "dir.c"}) {
                for (String dir3 : new String[]{".", "dir-a", "dir-b", "dir.c"}) {
                    for (String dir4 : new String[]{".", "dir-a", "dir-b", "dir.c"}) {
                        Files.createDirectories(fs.getPath(dir1.toString(), dir2, dir3, dir4));

                        for (String file : TEST_FILES_ALL) {
                            try {
                                Files.createFile(fs.getPath(dir1.toString(), dir2, dir3, dir4, file));
                            } catch (FileAlreadyExistsException ignore) {
                            }
                        }
                    }
                }
            }
        }

        return fs;
    }

    @ParameterizedTest(name = "{index} :: {1}")
    @MethodSource("data")
    public void test(Configuration fsConfig, String glob, Set<String> files, Set<String> paths) throws Exception {
        try (FileSystem fs = setUp(fsConfig)) {
            GlobFinder finder = new GlobFinder(fs, glob);

            assertEquals(
                    files.stream()
                            .map(fs::getPath)
                            .map(Path::normalize)
                            .map(Path::toAbsolutePath)
                            .collect(Collectors.toSet()),
                    StreamSupport.stream(finder.computeMatchingFiles().spliterator(), false)
                            .map(Path::normalize)
                            .map(Path::toAbsolutePath)
                            .collect(Collectors.toSet()),
                    String.format("Glob files: '%s'", glob)
            );

            assertEquals(
                    paths.stream()
                            .map(fs::getPath)
                            .map(Path::normalize)
                            .map(Path::toAbsolutePath)
                            .collect(Collectors.toSet()),
                    StreamSupport.stream(finder.computeMatchingPaths().spliterator(), false)
                            .map(Path::normalize)
                            .map(Path::toAbsolutePath)
                            .collect(Collectors.toSet()),
                    String.format("Glob paths: '%s'", glob)
            );
        }
    }
}
package io.github.spencerpark.jupyter.kernel.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
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

    private static Set<String> setOf(String... files) {
        return Arrays.stream(files).collect(Collectors.toSet());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { WIN_FS, "C:/*", allFilesMapped(s -> "C:/" + s) },
                { UNIX_FS, "/*", allFilesMapped(s -> "/" + s) },
                { OSX_FS, "/*", allFilesMapped(s -> "/" + s) },

                { WIN_FS, "C:/c.txt", setOf("C:/c.txt") },
                { UNIX_FS, "/c.txt", setOf("/c.txt") },
                { OSX_FS, "/c.txt", setOf("/c.txt") },

                { WIN_FS, "C:/*/c.txt", allDirsMapped(d -> "C:/" + d + "/c.txt") },
                { UNIX_FS, "/*/c.txt", allDirsMapped(d -> "/" + d + "/c.txt") },
                { OSX_FS, "/*/c.txt", allDirsMapped(d -> "/" + d + "/c.txt") },

                { WIN_FS, "C:/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "C:/dir-b/" + f) },
                { UNIX_FS, "/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f) },
                { OSX_FS, "/dir-b/*.txt", allFilesFilterMapped(f -> f.endsWith(".txt"), f -> "/dir-b/" + f) },

                { WIN_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f) },
                { UNIX_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f) },
                { OSX_FS, "*.pdf", allFilesFilterMapped(f -> f.endsWith(".pdf"), f -> "./" + f) },

                { WIN_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)) },
                { UNIX_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)) },
                { OSX_FS, "*/dir.c/*.svg", allDirsFlatMapped(d -> allFilesFilterMapped(f -> f.endsWith(".svg"), f -> "./" + d + "/dir.c/" + f)) },

                { WIN_FS, "?.pdf", setOf("C:/dir-a/a.pdf") },
                { UNIX_FS, "?.pdf", setOf("/dir-a/a.pdf") },
                { OSX_FS, "?.pdf", setOf("/dir-a/a.pdf") },

                { WIN_FS, "C:/dir-?/?.pdf", setOf("C:/dir-a/a.pdf", "C:/dir-b/a.pdf") },
                { UNIX_FS, "/dir-?/?.pdf", setOf("/dir-a/a.pdf", "/dir-b/a.pdf") },
                { OSX_FS, "/dir-?/?.pdf", setOf("/dir-a/a.pdf", "/dir-b/a.pdf") },

                { WIN_FS, "C:/dir.c/abc.svg", setOf("C:/dir.c/abc.svg") },
                { UNIX_FS, "/dir.c/abc.svg", setOf("/dir.c/abc.svg") },
                { OSX_FS, "/dir.c/abc.svg", setOf("/dir.c/abc.svg") },

                { WIN_FS, "C:/bad", Collections.emptySet() },
                { UNIX_FS, "/bad", Collections.emptySet() },
                { OSX_FS, "/bad", Collections.emptySet() },

                { WIN_FS, "C:/*/*/*/*/*/*/*", Collections.emptySet() },
                { UNIX_FS, "/*/*/*/*/*/*/*", Collections.emptySet() },
                { OSX_FS, "/*/*/*/*/*/*/*", Collections.emptySet() },
        });
    }

    private final Configuration fsConfig;
    private final String glob;
    private final Set<String> files;

    private FileSystem fs;

    public GlobFinderTest(Configuration fsConfig, String glob, Set<String> files) {
        this.fsConfig = fsConfig;
        this.glob = glob;
        this.files = files;
    }

    @Before
    public void setUp() throws Exception {
        this.fs = Jimfs.newFileSystem(this.fsConfig);

        List<Path> roots = StreamSupport.stream(this.fs.getRootDirectories().spliterator(), false).collect(Collectors.toList());
        roots.add(this.fs.getPath("."));

        for (Path dir1 : roots) {
            for (String dir2 : new String[]{ ".", "dir-a", "dir-b", "dir.c" }) {
                for (String dir3 : new String[]{ ".", "dir-a", "dir-b", "dir.c" }) {
                    for (String dir4 : new String[]{ ".", "dir-a", "dir-b", "dir.c" }) {
                        Files.createDirectories(this.fs.getPath(dir1.toString(), dir2, dir3, dir4));

                        for (String file : TEST_FILES_ALL) {
                            try {
                                Files.createFile(this.fs.getPath(dir1.toString(), dir2, dir3, dir4, file));
                            } catch (FileAlreadyExistsException ignore) {}
                        }
                    }
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        this.fs = null;
    }

    @Test
    public void test() throws Exception {
        GlobFinder finder = new GlobFinder(this.fs, this.glob);
        Iterable<Path> paths = finder.computeMatchingPaths();

        assertEquals(
                String.format("Glob: '%s'", this.glob),
                this.files.stream()
                        .map(this.fs::getPath)
                        .map(Path::normalize)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toSet()),
                StreamSupport.stream(paths.spliterator(), false)
                        .map(Path::normalize)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toSet())
        );
    }
}
package io.github.spencerpark.jupyter.kernel.extension;

import io.github.spencerpark.jupyter.api.util.GlobFinder;
import io.github.spencerpark.jupyter.spi.JupyterKernelExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class JupyterKernelExtensionClassLoader extends URLClassLoader {
    private static URL toURL(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            // Should only happen if we somehow find ourselves without support for the file protocol. We aren't
            // recovering from that...
            throw new RuntimeException(e);
        }
    }

    private static boolean isJarPath(Path path) {
        return path.endsWith(".jar") && Files.isRegularFile(path);
    }

    /**
     * Initializes classloaders for all extensions in the extension path provided. All entries in the path are path
     * glob strings which are {@link GlobFinder#computeMatchingPaths() resolved to a path set}. Each directory in the
     * resolved matching set get their own loader, as do jars.
     *
     * e.g. if the `fs` contains a folder with the following structure:
     * <pre>
     * ext/
     *   my-ext/
     *     my-ext-1.0.0.jar
     *   my-exploded-ext/
     *     io/
     *       github/...
     *         MyExt.class
     *     META-INF/
     *       services/
     *         io.github.spencerpark.jupyter.spi.JupyterExtension
     *   my-other-ext-1.0.0.jar
     * </pre>
     * then the path `ext/*` would return a classloader for `my-ext/`, `my-exploded-ext/`, and `my-other-ext-1.0.0.jar`.
     *
     * @param fs     the file system on which to resolve the globs.
     * @param path   the extension path.
     * @param parent the parent classloader for all returned classloaders.
     *
     * @return classloaders for each extension referenced by the path.
     */
    public static Collection<JupyterKernelExtensionClassLoader> ofExtensionPath(FileSystem fs, Iterable<String> path, ClassLoader parent) {
        Collection<JupyterKernelExtensionClassLoader> loaders = new LinkedList<>();
        for (String entry : path) {
            try {
                for (Path ext : new GlobFinder(fs, entry).computeMatchingPaths()) {
                    if (Files.isDirectory(ext)) {
                        try {
                            loaders.add(JupyterKernelExtensionClassLoader.ofDirectory(ext, parent));
                        } catch (IOException ignored) { }
                    } else if (isJarPath(ext)) {
                        loaders.add(JupyterKernelExtensionClassLoader.ofJarPath(ext, parent));
                    }
                }
            } catch (IOException ignored) { }
        }
        return loaders;
    }

    public static JupyterKernelExtensionClassLoader ofDirectory(Path extDir, ClassLoader parent) throws IOException {
        try (Stream<Path> jarPaths = Files.list(extDir).filter(JupyterKernelExtensionClassLoader::isJarPath)) {
            URL[] classpath = Stream.concat(Stream.of(extDir), jarPaths)
                    .map(JupyterKernelExtensionClassLoader::toURL)
                    .toArray(URL[]::new);

            return new JupyterKernelExtensionClassLoader(extDir, classpath, parent);
        }
    }

    public static JupyterKernelExtensionClassLoader ofJarPath(Path jarPath, ClassLoader parent) {
        return new JupyterKernelExtensionClassLoader(jarPath, new URL[]{ toURL(jarPath) }, parent);
    }

    private final Path root;

    private JupyterKernelExtensionClassLoader(Path root, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.root = root;
    }

    public Path getRoot() {
        return root;
    }

    public ServiceLoader<JupyterKernelExtension> loadExtensions() {
        return ServiceLoader.load(JupyterKernelExtension.class, this);
    }
}

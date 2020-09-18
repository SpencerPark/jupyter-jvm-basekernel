package io.github.spencerpark.jupyter.kernel.extension;

import io.github.spencerpark.jupyter.api.JupyterKernel;
import io.github.spencerpark.jupyter.api.util.GlobFinder;
import io.github.spencerpark.jupyter.spi.JupyterKernelExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class JupyterKernelExtensions {
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
     * @param action the action to preform on every (root, containerClassLoader) pair in the {@code path}.
     */
    public static void forEachExtensionContainer(FileSystem fs, Iterable<String> path, ClassLoader parent, BiConsumer<Path, ClassLoader> action) {
        for (String entry : path) {
            try {
                for (Path ext : new GlobFinder(fs, entry).computeMatchingPaths()) {
                    if (Files.isDirectory(ext)) {
                        try {
                            action.accept(ext, createLoaderForDirectory(ext, parent));
                        } catch (IOException ignored) { }
                    } else if (isJarPath(ext)) {
                        action.accept(ext, createLoaderFromJar(ext, parent));
                    }
                }
            } catch (IOException ignored) { }
        }
    }

    public static ContainerClassLoader createLoaderForDirectory(Path extDir, ClassLoader parent) throws IOException {
        try (Stream<Path> jarPaths = Files.list(extDir).filter(JupyterKernelExtensions::isJarPath)) {
            URL[] classpath = Stream.concat(Stream.of(extDir), jarPaths)
                    .map(JupyterKernelExtensions::toURL)
                    .toArray(URL[]::new);

            return new ContainerClassLoader(classpath, parent);
        }
    }

    public static ContainerClassLoader createLoaderFromJar(Path jarPath, ClassLoader parent) {
        return new ContainerClassLoader(new URL[]{ toURL(jarPath) }, parent);
    }

    private final JupyterKernel owner;

    private final Map<JupyterKernelExtension, ClassLoader> loaded = new IdentityHashMap<>();

    public JupyterKernelExtensions(JupyterKernel owner) {
        this.owner = owner;
    }

    public List<JupyterKernelExtensionLoadException> loadAll(Iterable<String> extensionPath, ClassLoader parent) {
        List<JupyterKernelExtensionLoadException> failedToLoad = new LinkedList<>();
        JupyterKernelExtensions.forEachExtensionContainer(FileSystems.getDefault(), extensionPath, parent, (root, loader) -> {
            Iterator<JupyterKernelExtension> iterator = ServiceLoader.load(JupyterKernelExtension.class, loader).iterator();
            while (iterator.hasNext()) {
                try {
                    JupyterKernelExtension ext = iterator.next();
                    loaded.computeIfAbsent(ext, this.loadingWith(root, loader, failedToLoad));
                } catch (Throwable t) {
                    failedToLoad.add(new JupyterKernelExtensionLoadException(
                            String.format("Error loading service from %s.", root.toUri()), t));
                }
            }
        });
        return failedToLoad;
    }

    private Function<JupyterKernelExtension, ClassLoader> loadingWith(
            Path root,
            ClassLoader loader,
            List<JupyterKernelExtensionLoadException> failedToLoad
    ) {
        return ext -> {
            try {
                ext.load(this.owner);
                return loader;
            } catch (Throwable e) {
                failedToLoad.add(new JupyterKernelExtensionLoadException(
                        String.format("Error loading extension %s from %s.",
                                ext.getClass().getCanonicalName(),
                                root.toUri()
                        ), e));
                return null;
            }
        };
    }
}

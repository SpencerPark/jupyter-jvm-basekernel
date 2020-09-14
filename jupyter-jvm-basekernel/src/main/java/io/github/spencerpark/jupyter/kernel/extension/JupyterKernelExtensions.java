package io.github.spencerpark.jupyter.kernel.extension;

import io.github.spencerpark.jupyter.api.JupyterKernel;
import io.github.spencerpark.jupyter.spi.JupyterKernelExtension;

import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;

public class JupyterKernelExtensions {
    private final JupyterKernel owner;

    private final Map<JupyterKernelExtension, JupyterKernelExtensionClassLoader> loaded = new IdentityHashMap<>();

    public JupyterKernelExtensions(JupyterKernel owner) {
        this.owner = owner;
    }

    public List<JupyterKernelExtensionLoadException> loadAll(Iterable<String> extensionPath, ClassLoader parent) {
        List<JupyterKernelExtensionLoadException> failedToLoad = new LinkedList<>();
        JupyterKernelExtensionClassLoader.ofExtensionPath(FileSystems.getDefault(), extensionPath, parent).forEach(loader -> {
            Iterator<JupyterKernelExtension> iterator = loader.loadExtensions().iterator();
            while (iterator.hasNext()) {
                try {
                    JupyterKernelExtension ext = iterator.next();
                    loaded.computeIfAbsent(ext, loadingWith(loader, failedToLoad));
                } catch (Throwable t) {
                    failedToLoad.add(new JupyterKernelExtensionLoadException(
                            String.format("Error loading service from %s.",
                                    loader.getRoot().toUri()
                            ), t));
                }
            }
        });
        return failedToLoad;
    }

    private Function<JupyterKernelExtension, JupyterKernelExtensionClassLoader> loadingWith(
            JupyterKernelExtensionClassLoader loader,
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
                                loader.getRoot().toUri()
                        ), e));
                return null;
            }
        };
    }
}

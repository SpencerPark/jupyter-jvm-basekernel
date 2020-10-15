package io.github.spencerpark.jupyter.kernel.extension;

import java.net.URL;
import java.net.URLClassLoader;

public class ContainerClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final ClassLoader parent;

    /**
     * Behaviour varies slightly, a null {@code parent} will actually use the {@link ClassLoader#getSystemClassLoader()
     * system class loader}.
     */
    public ContainerClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent != null ? parent : ClassLoader.getSystemClassLoader());
        this.parent = this.getParent();
    }

    @Override
    public URL getResource(String name) {
        // Alternate order, search this class loader first before searching the parent. We don't
        // call `super.getResource(name)` as a missing resource in the parent would then search
        // this loader again unnecessarily.
        URL url = this.findResource(name);
        return url != null ? url : this.parent.getResource(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(name)) {
            // Always check if loaded first, regardless of the load order.
            Class<?> clazz = this.findLoadedClass(name);

            // If not loaded, then look it up.
            if (clazz == null) {
                try {
                    // First check this loader.
                    clazz = this.findClass(name);
                } catch (ClassNotFoundException e) {
                    // Thrown by findClass if the lookup fails, in this case we defer to
                    // the parent loader.
                    // Relying on parent to properly implement the impl note dictating
                    // that this is equivalent to `loadClass(name, false)`.
                    clazz = this.parent.loadClass(name);
                }
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }
}

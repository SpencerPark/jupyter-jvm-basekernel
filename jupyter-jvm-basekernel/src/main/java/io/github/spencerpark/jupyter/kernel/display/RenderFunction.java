package io.github.spencerpark.jupyter.kernel.display;

@FunctionalInterface
public interface RenderFunction<T> {
    void render(T data, RenderContext context);
}

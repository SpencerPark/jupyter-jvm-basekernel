package io.github.spencerpark.jupyter.api.display;

@FunctionalInterface
public interface RenderFunction<T> {
    void render(T data, RenderContext context);
}

package io.github.spencerpark.jupyter.ipywidgets;

@FunctionalInterface
public interface StatePatch<S> {
    public S apply(S original);
}

package io.github.spencerpark.jupyter.spi;

import io.github.spencerpark.jupyter.api.JupyterKernel;

public interface JupyterExtension {
    void load(JupyterKernel kernel);
}

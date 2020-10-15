package io.github.spencerpark.jupyter.client.api;

import java.util.List;

public interface JupyterError {

    public String getErrorName();

    public String getErrorMessage();

    public List<String> getStacktrace();
}

package io.github.spencerpark.jupyter.client.api;

public class JupyterPublishedException extends JupyterRuntimeException {
    protected final JupyterError wrapped;

    public JupyterPublishedException(JupyterError wrapped) {
        super(String.format("Jupyter published error: %s. %s", wrapped.getErrorName(), wrapped.getErrorMessage()));
        this.wrapped = wrapped;
    }

    public JupyterError unwrap() {
        return this.wrapped;
    }
}

package io.github.spencerpark.jupyter.client.api;

public class JupyterReplyException extends JupyterRuntimeException {
    protected final JupyterError wrapped;

    public JupyterReplyException(JupyterError wrapped) {
        super(String.format("Jupyter reply error: %s. %s", wrapped.getErrorName(), wrapped.getErrorMessage()));
        this.wrapped = wrapped;
    }

    public JupyterError unwrap() {
        return this.wrapped;
    }
}

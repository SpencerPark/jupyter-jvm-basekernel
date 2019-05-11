package io.github.spencerpark.jupyter.client;

public class JupyterRuntimeException extends RuntimeException {
    public JupyterRuntimeException() {
    }

    public JupyterRuntimeException(String message) {
        super(message);
    }

    public JupyterRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JupyterRuntimeException(Throwable cause) {
        super(cause);
    }
}

package io.github.spencerpark.jupyter.client;

public class JupyterProtocolException extends JupyterRuntimeException {
    public JupyterProtocolException() {
    }

    public JupyterProtocolException(String message) {
        super(message);
    }

    public JupyterProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public JupyterProtocolException(Throwable cause) {
        super(cause);
    }
}

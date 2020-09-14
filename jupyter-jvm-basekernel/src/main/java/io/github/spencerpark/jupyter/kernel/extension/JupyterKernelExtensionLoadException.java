package io.github.spencerpark.jupyter.kernel.extension;

public class JupyterKernelExtensionLoadException extends RuntimeException {
    public JupyterKernelExtensionLoadException() {
        super();
    }

    public JupyterKernelExtensionLoadException(String message) {
        super(message);
    }

    public JupyterKernelExtensionLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public JupyterKernelExtensionLoadException(Throwable cause) {
        super(cause);
    }
}

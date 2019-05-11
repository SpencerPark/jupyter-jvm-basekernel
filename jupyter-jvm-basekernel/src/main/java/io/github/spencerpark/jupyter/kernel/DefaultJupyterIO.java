package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.api.JupyterIO;
import io.github.spencerpark.jupyter.channels.JupyterInputStream;
import io.github.spencerpark.jupyter.channels.JupyterOutputStream;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class DefaultJupyterIO extends JupyterIO {
    private static PrintStream makeStream(JupyterOutputStream jupyterStream, Charset encoding) {
        try {
            return new PrintStream(jupyterStream, true, encoding.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't lookup the charset by name even though it is already a charset...", e);
        }
    }

    private final DefaultDisplayStream jupyterDisplay;
    private final JupyterOutputStream jupyterOut;
    private final JupyterOutputStream jupyterErr;
    private final JupyterInputStream jupyterIn;

    private DefaultJupyterIO(Charset encoding, DefaultDisplayStream display, JupyterOutputStream out, JupyterOutputStream err, JupyterInputStream in) {
        super(display, makeStream(out, encoding), makeStream(err, encoding), in);

        this.jupyterDisplay = display;
        this.jupyterOut = out;
        this.jupyterErr = err;
        this.jupyterIn = in;
    }

    public DefaultJupyterIO(Charset encoding) {
        this(
                encoding,
                new DefaultDisplayStream(),
                new JupyterOutputStream(ShellReplyEnvironment::writeToStdOut),
                new JupyterOutputStream(ShellReplyEnvironment::writeToStdErr),
                new JupyterInputStream(encoding)
        );
    }

    public DefaultJupyterIO() {
        this(JupyterSocket.UTF_8);
    }

    @Override
    public boolean isAttached() {
        return this.jupyterOut.isAttached()
                && this.jupyterErr.isAttached()
                && this.jupyterIn.isAttached()
                && this.jupyterDisplay.isAttached();
    }

    protected void setEnv(ShellReplyEnvironment env) {
        this.jupyterOut.setEnv(env);
        this.jupyterErr.setEnv(env);
        this.jupyterIn.setEnv(env);
        this.jupyterDisplay.setEnv(env);
    }

    protected void retractEnv(ShellReplyEnvironment env) {
        this.jupyterOut.retractEnv(env);
        this.jupyterErr.retractEnv(env);
        this.jupyterIn.retractEnv(env);
        this.jupyterDisplay.retractEnv(env);
    }

    protected void setJupyterInEnabled(boolean enabled) {
        this.jupyterIn.setEnabled(enabled);
    }
}

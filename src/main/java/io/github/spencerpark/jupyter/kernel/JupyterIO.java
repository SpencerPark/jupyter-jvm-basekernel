package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.channels.JupyterInputStream;
import io.github.spencerpark.jupyter.channels.JupyterOutputStream;
import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;

import java.io.InputStream;
import java.io.PrintStream;

public class JupyterIO {
    private final JupyterOutputStream jupyterOut;
    private final JupyterOutputStream jupyterErr;
    private final JupyterInputStream jupyterIn;

    public final DisplayStream display;

    public final PrintStream out;
    public final PrintStream err;
    public final InputStream in;

    public JupyterIO() {
        this.jupyterOut = new JupyterOutputStream(true);
        this.jupyterErr = new JupyterOutputStream(false);
        this.jupyterIn = new JupyterInputStream();

        this.display = new DisplayStream();

        this.out = new PrintStream(this.jupyterOut, true);
        this.err = new PrintStream(this.jupyterErr, true);
        this.in = this.jupyterIn;
    }

    public boolean isAttached() {
        return this.jupyterOut.isAttached()
                && this.jupyterErr.isAttached()
                && this.jupyterIn.isAttached()
                && this.display.isAttached();
    }

    protected void setEnv(ShellReplyEnvironment env) {
        this.jupyterOut.setEnv(env);
        this.jupyterErr.setEnv(env);
        this.jupyterIn.setEnv(env);
        this.display.setEnv(env);
    }

    protected void retractEnv(ShellReplyEnvironment env) {
        this.jupyterOut.retractEnv(env);
        this.jupyterErr.retractEnv(env);
        this.jupyterIn.retractEnv(env);
        this.display.retractEnv(env);
    }

    protected void setJupyterInEnabled(boolean enabled) {
        this.jupyterIn.setEnabled(enabled);
    }
}

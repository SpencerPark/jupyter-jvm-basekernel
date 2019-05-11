package io.github.spencerpark.jupyter.api;

import java.io.InputStream;
import java.io.PrintStream;

public abstract class JupyterIO {
    public final DisplayStream display;

    public final PrintStream out;
    public final PrintStream err;
    public final InputStream in;

    public JupyterIO(DisplayStream display, PrintStream out, PrintStream err, InputStream in) {
        this.display = display;
        this.out = out;
        this.err = err;
        this.in = in;
    }

    public abstract boolean isAttached();
}

package io.github.spencerpark.jupyter.channels;

import java.io.ByteArrayOutputStream;

public class JupyterOutputStream extends ByteArrayOutputStream {
    private static final int INITIAL_BUFFER_CAP = 1024;

    private ShellReplyEnvironment env;
    private final boolean isOut;

    public JupyterOutputStream(boolean isStdOut) {
        this(null, isStdOut);
    }

    public JupyterOutputStream(ShellReplyEnvironment env, boolean isStdOut) {
        super(INITIAL_BUFFER_CAP);
        this.env = env;
        this.isOut = isStdOut;
    }

    public void setEnv(ShellReplyEnvironment env) {
        this.env = env;
    }

    public void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    @Override
    public void flush() {
        if (this.env != null) {
            String contents = super.toString();
            if (this.isOut)
                this.env.writeToStdOut(contents);
            else
                this.env.writeToStdErr(contents);
        }

        super.reset();
    }
}

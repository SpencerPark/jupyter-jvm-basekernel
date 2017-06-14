package io.github.spencerpark.jupyter.channels;

import java.io.IOException;
import java.io.InputStream;

public class JupyterInputStream extends InputStream {
    private ShellReplyEnvironment env;
    private byte[] data = null;
    private int bufferPos = 0;

    public JupyterInputStream() {
        this.env = null;
    }

    public JupyterInputStream(ShellReplyEnvironment env) {
        this.env = env;
    }

    public void setEnv(ShellReplyEnvironment env) {
        this.env = env;
    }

    public void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    @Override
    public synchronized int read() throws IOException {
        if (this.data == null) {
            if (this.env != null) {
                //Buffer is empty and there is an environment to read from so
                //ask the frontend for input
                this.data = this.env.readFromStdIn().getBytes();
                this.bufferPos = 0;
            } else {
                return -1;
            }
        }
        if (this.bufferPos >= this.data.length) {
            this.data = null;
            return -1;
        }

        return this.data[this.bufferPos++];
    }
}

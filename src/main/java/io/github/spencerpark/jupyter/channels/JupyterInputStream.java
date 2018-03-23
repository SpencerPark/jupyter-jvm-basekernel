package io.github.spencerpark.jupyter.channels;

import java.io.IOException;
import java.io.InputStream;

public class JupyterInputStream extends InputStream {
    private ShellReplyEnvironment env;
    private boolean enabled;
    private byte[] data = null;
    private int bufferPos = 0;

    public JupyterInputStream() {
        this.env = null;
        this.enabled = false;
    }

    public JupyterInputStream(ShellReplyEnvironment env, boolean enabled) {
        this.env = env;
        this.enabled = enabled;
    }

    public void setEnv(ShellReplyEnvironment env) {
        this.env = env;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    private byte[] readFromFrontend() {
        if (this.enabled)
            return this.env.readFromStdIn().getBytes();
        return new byte[0];
    }

    @Override
    public synchronized int read() throws IOException {
        if (this.data == null) {
            if (this.env != null) {
                //Buffer is empty and there is an environment to read from so
                //ask the frontend for input
                this.data = this.readFromFrontend();
                this.bufferPos = 0;
            } else {
                return -1;
            }
        }
        if (this.bufferPos >= this.data.length) {
            this.data = null;
            if (this.env != null && this.enabled) {
                this.data = this.readFromFrontend();
                this.bufferPos = 0;
            } else {
                return -1;
            }
        }

        return this.data[this.bufferPos++];
    }
}

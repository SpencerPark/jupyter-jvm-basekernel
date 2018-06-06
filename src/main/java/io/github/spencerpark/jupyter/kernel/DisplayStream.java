package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.publish.PublishDisplayData;

public class DisplayStream {
    private ShellReplyEnvironment env;

    protected void setEnv(ShellReplyEnvironment env) {
        this.env = env;
    }

    protected void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    public boolean isAttached() {
        return this.env != null;
    }

    public void display(DisplayData data) {
        if (this.env != null)
            this.env.publish(new PublishDisplayData(data));
    }
}

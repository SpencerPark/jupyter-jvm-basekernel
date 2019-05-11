package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.github.spencerpark.jupyter.messages.publish.PublishDisplayData;
import io.github.spencerpark.jupyter.messages.publish.PublishUpdateDisplayData;

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

    public void updateDisplay(DisplayData data) {
        if (!data.hasDisplayId())
            throw new IllegalArgumentException("Data must have a display_id in order to update an existing display.");

        if (this.env != null)
            this.env.publish(new PublishUpdateDisplayData(data));
    }

    public void updateDisplay(String id, DisplayData data) {
        data.setDisplayId(id);
        this.updateDisplay(data);
    }
}

package io.github.spencerpark.jupyter.kernel;

import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.kernel.debugger.DapEventPublisher;
import io.github.spencerpark.jupyter.messages.publish.PublishDebugEvent;

public class JupyterDebugEventPublisher implements DapEventPublisher {
    private ShellReplyEnvironment env;

    public JupyterDebugEventPublisher(ShellReplyEnvironment env) {
        this.env = env;
    }

    // TODO should be kept after the reply it seems? Or maybe parent doesn't matter and python just uses
    //  the most recent one.
    protected void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    public boolean isAttached() {
        return this.env != null;
    }

    @Override
    public void emit(JsonElement dapEvent) {
        if (this.env != null) {
            this.env.publish(new PublishDebugEvent(dapEvent));
        }
    }
}

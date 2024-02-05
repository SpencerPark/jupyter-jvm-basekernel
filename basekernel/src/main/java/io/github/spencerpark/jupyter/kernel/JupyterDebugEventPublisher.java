package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.kernel.debugger.DapEventPublisher;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;
import io.github.spencerpark.jupyter.messages.publish.PublishDebugEvent;

public class JupyterDebugEventPublisher implements DapEventPublisher {
    private ShellReplyEnvironment env;

    public JupyterDebugEventPublisher(ShellReplyEnvironment env) {
        this.env = env;
    }

    protected void retractEnv(ShellReplyEnvironment env) {
        if (this.env == env)
            this.env = null;
    }

    public boolean isAttached() {
        return this.env != null;
    }

    @Override
    public void emit(JsonBox.Wrapper dapEvent) {
        if (this.env != null) {
            this.env.publish(new PublishDebugEvent(dapEvent));
        }
    }
}

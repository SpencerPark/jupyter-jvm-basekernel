package io.github.spencerpark.jupyter.kernel.debugger;

import io.github.spencerpark.jupyter.messages.adapters.JsonBox;

public interface Debugger {

    JsonBox.Wrapper handleDapRequest(DapEventPublisher pub, JsonBox.Wrapper dapRequest);
}

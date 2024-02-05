package io.github.spencerpark.jupyter.kernel.debugger;

import io.github.spencerpark.jupyter.messages.adapters.JsonBox;

public interface DapEventPublisher {
    void emit(JsonBox.Wrapper dapEvent);
}

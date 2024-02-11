package io.github.spencerpark.jupyter.kernel.debugger;

import com.google.gson.JsonElement;

public interface DapEventPublisher {
    void emit(JsonElement dapEvent);
}

package io.github.spencerpark.jupyter.kernel.debugger;

import com.google.gson.JsonElement;

public interface Debugger {
    Runnable subscribe(DapEventPublisher pub);

    JsonElement handleDapRequest(JsonElement dapRequest);
}

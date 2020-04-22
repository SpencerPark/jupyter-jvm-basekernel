package io.github.spencerpark.jupyter.client.handlers;

import io.github.spencerpark.jupyter.client.api.JupyterError;
import io.github.spencerpark.jupyter.messages.publish.PublishError;

import java.util.Collections;
import java.util.List;

class PublishErrorAdapter implements JupyterError {
    private final PublishError error;

    public PublishErrorAdapter(PublishError error) {
        this.error = error;
    }

    @Override
    public String getErrorName() {
        return this.error.getErrorName();
    }

    @Override
    public String getErrorMessage() {
        return this.error.getErrorMessage();
    }

    @Override
    public List<String> getStacktrace() {
        return Collections.unmodifiableList(this.error.getStacktrace());
    }
}

package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.messages.publish.PublishError;

public class JupyterPublishedException extends JupyterRuntimeException {
    private final PublishError published;

    public JupyterPublishedException(PublishError published) {
        this.published = published;
    }

    public PublishError getPublishedError() {
        return this.published;
    }
}

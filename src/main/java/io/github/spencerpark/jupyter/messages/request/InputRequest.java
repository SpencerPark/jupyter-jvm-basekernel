package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class InputRequest implements ContentType<InputRequest> {
    public static final MessageType<InputRequest> MESSAGE_TYPE = MessageType.INPUT_REQUEST;

    @Override
    public MessageType<InputRequest> getType() {
        return MESSAGE_TYPE;
    }

    protected String prompt;
    protected boolean password;

    public InputRequest(String prompt, boolean password) {
        this.prompt = prompt;
        this.password = password;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean isPassword() {
        return password;
    }
}

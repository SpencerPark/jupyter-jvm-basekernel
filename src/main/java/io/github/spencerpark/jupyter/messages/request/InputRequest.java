package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.MessageType;

public class InputRequest {
    public static final MessageType<InputRequest> MESSAGE_TYPE = MessageType.INPUT_REQUEST;

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

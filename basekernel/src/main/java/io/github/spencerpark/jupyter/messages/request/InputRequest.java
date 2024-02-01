package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.RequestType;
import io.github.spencerpark.jupyter.messages.reply.InputReply;

public class InputRequest implements ContentType<InputRequest>, RequestType<InputReply> {
    public static final MessageType<InputRequest> MESSAGE_TYPE = MessageType.INPUT_REQUEST;
    public static final MessageType<InputReply> REPLY_MESSAGE_TYPE = MessageType.INPUT_REPLY;

    @Override
    public MessageType<InputRequest> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<InputReply> getReplyType() {
        return REPLY_MESSAGE_TYPE;
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

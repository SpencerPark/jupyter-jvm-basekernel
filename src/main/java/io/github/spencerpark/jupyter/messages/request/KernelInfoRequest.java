package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;

public class KernelInfoRequest implements ContentType<KernelInfoRequest> {
    public static final MessageType<KernelInfoRequest> MESSAGE_TYPE = MessageType.KERNEL_INFO_REQUEST;

    @Override
    public MessageType<KernelInfoRequest> getType() {
        return MESSAGE_TYPE;
    }
}

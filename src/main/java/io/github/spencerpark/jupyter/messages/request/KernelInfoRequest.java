package io.github.spencerpark.jupyter.messages.request;

import io.github.spencerpark.jupyter.messages.MessageType;

public class KernelInfoRequest {
    public static final MessageType<KernelInfoRequest> MESSAGE_TYPE = MessageType.KERNEL_INFO_REQUEST;
}

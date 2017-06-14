package io.github.spencerpark.jupyter.messages;

import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageType<T> {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private static final Map<String, MessageType<?>> TYPE_BY_NAME = new HashMap<>();

    public static MessageType<?> getType(String name) {
        MessageType<?> type = TYPE_BY_NAME.get(name);
        return type == null ? UNKNOWN : type;
    }

    //Request
    public static final MessageType<ExecuteRequest> EXECUTE_REQUEST = new MessageType<>("execute_request", ExecuteRequest.class);
    public static final MessageType<InspectRequest> INSPECT_REQUEST = new MessageType<>("inspect_request", InspectRequest.class);
    public static final MessageType<CompleteRequest> COMPLETE_REQUEST = new MessageType<>("complete_request", CompleteRequest.class);
    public static final MessageType<HistoryRequest> HISTORY_REQUEST = new MessageType<>("history_request", HistoryRequest.class);
    public static final MessageType<IsCompleteRequest> IS_COMPLETE_REQUEST = new MessageType<>("is_complete_request", IsCompleteRequest.class);
    public static final MessageType<CommInfoRequest> COMM_INFO_REQUEST = new MessageType<>("comm_info_request", CommInfoRequest.class);
    public static final MessageType<KernelInfoRequest> KERNEL_INFO_REQUEST = new MessageType<>("kernel_info_request", KernelInfoRequest.class);
    public static final MessageType<ShutdownRequest> SHUTDOWN_REQUEST = new MessageType<>("shutdown_request", ShutdownRequest.class);

    //Reply
    public static final MessageType<ExecuteReply> EXECUTE_REPLY = new MessageType<>("execute_reply", ExecuteReply.class);
    public static final MessageType<InspectReply> INSPECT_REPLY = new MessageType<>("inspect_reply", InspectReply.class);
    public static final MessageType<CompleteReply> COMPLETE_REPLY = new MessageType<>("complete_reply", CompleteReply.class);
    public static final MessageType<HistoryReply> HISTORY_REPLY = new MessageType<>("history_reply", HistoryReply.class);
    public static final MessageType<IsCompleteReply> IS_COMPLETE_REPLY = new MessageType<>("is_complete_reply", IsCompleteReply.class);
    public static final MessageType<CommInfoReply> COMM_INFO_REPLY = new MessageType<>("comm_info_reply", CommInfoReply.class);
    public static final MessageType<KernelInfoReply> KERNEL_INFO_REPLY = new MessageType<>("kernel_info_reply", KernelInfoReply.class);
    public static final MessageType<ShutdownReply> SHUTDOWN_REPLY = new MessageType<>("shutdown_reply", ShutdownReply.class);

    //Publish
    public static final MessageType<PublishStream> PUBLISH_STREAM = new MessageType<>("stream", PublishStream.class);
    public static final MessageType<PublishDisplayData> PUBLISH_DISPLAY_DATA = new MessageType<>("display_data", PublishDisplayData.class);
    //TODO update_display_data type
    public static final MessageType<PublishExecuteInput> PUBLISH_EXECUTE_INPUT = new MessageType<>("execute_input", PublishExecuteInput.class);
    public static final MessageType<PublishExecuteResult> PUBLISH_EXECUTION_RESULT = new MessageType<>("execute_result", PublishExecuteResult.class);
    public static final MessageType<PublishError> PUBLISH_ERROR = new MessageType<>("error", PublishError.class);
    public static final MessageType<PublishStatus> PUBLISH_STATUS = new MessageType<>("status", PublishStatus.class);
    public static final MessageType<PublishClearOutput> PUBLISH_CLEAR_OUTPUT = new MessageType<>("clear_output", PublishClearOutput.class);

    //Stdin
    public static final MessageType<InputRequest> INPUT_REQUEST = new MessageType<>("input_request", InputRequest.class);

    public static final MessageType<InputReply> INPUT_REPLY = new MessageType<>("input_reply", InputReply.class);

    public static final MessageType<?> UNKNOWN = new MessageType<>("none", Object.class);

    private final String name;
    private final Class<T> contentType;
    private final int id;
    private final MessageType<ErrorReply> errorType;

    private MessageType(String name, Class<T> contentType) {
        this(name, contentType, false);
    }

    private MessageType(String name, Class<T> contentType, boolean isErrorType) {
        this.name = name;
        this.contentType = contentType;
        this.id = NEXT_ID.getAndIncrement();
        if (!isErrorType) TYPE_BY_NAME.put(name, this);
        if (!isErrorType) this.errorType = new MessageType<>(name, ErrorReply.class, true);
        else this.errorType = null;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getContentType() {
        return this.contentType;
    }

    public MessageType<ErrorReply> error() {
        return this.errorType;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}

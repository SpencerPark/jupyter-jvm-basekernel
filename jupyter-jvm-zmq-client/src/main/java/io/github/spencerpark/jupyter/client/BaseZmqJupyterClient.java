package io.github.spencerpark.jupyter.client;

import io.github.spencerpark.jupyter.channels.ReplyEnvironment;
import io.github.spencerpark.jupyter.client.api.IOProvider;
import io.github.spencerpark.jupyter.client.api.JupyterKernelDiedException;
import io.github.spencerpark.jupyter.client.api.JupyterProtocolException;
import io.github.spencerpark.jupyter.client.channels.JupyterClientConnection;
import io.github.spencerpark.jupyter.client.channels.ShellClientChannel;
import io.github.spencerpark.jupyter.client.handlers.ReplyHandler;
import io.github.spencerpark.jupyter.client.handlers.ShellReplyHandler;
import io.github.spencerpark.jupyter.comm.CommClient;
import io.github.spencerpark.jupyter.comm.DefaultCommClient;
import io.github.spencerpark.jupyter.comm.DefaultCommManager;
import io.github.spencerpark.jupyter.comm.DefaultCommServer;
import io.github.spencerpark.jupyter.messages.*;
import io.github.spencerpark.jupyter.messages.publish.*;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.InputRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseZmqJupyterClient implements AutoCloseable {
    private final DefaultCommManager commManager;

    private JupyterClientConnection connection;
    private final Map<String, ShellReplyHandler<?>> activeReplyHandlers = new ConcurrentHashMap<>();

    public BaseZmqJupyterClient(DefaultCommManager commManager) {
        this.commManager = commManager;
    }

    public BaseZmqJupyterClient() {
        this(new DefaultCommManager());
    }

    // API

    protected abstract void handleNotifyOfExecutingCode(PublishExecuteInput input);

    protected abstract void handleKernelStatusChange(PublishStatus status);

    protected abstract <T> ReplyHandler<T> getWildReplyHandler();

    public <Req extends ContentType<Req>&RequestType<Rep>, Rep> ShellReplyHandler<Rep> performShellRequest(Req content, IOProvider ioProvider) {
        if (this.connection == null)
            throw new IllegalStateException("Client not connected.");

        // Initialize a promise that will complete when the handler receives a reply.
        ShellReplyHandler<Rep> handler = new ShellReplyHandler<>(content.getReplyType(), ioProvider);

        // Initialize the request.
        Header<Req> header = new Header<>(content.getType());
        Message<Req> message = new Message<>(header, content);
        String reqId = header.getId();

        // Register the handler so that it captures io, etc, while the kernel is processing
        // the request and receives the reply.
        this.activeReplyHandlers.put(reqId, handler);
        handler.getFutureResult().whenComplete((res, ex) ->
                this.activeReplyHandlers.remove(reqId, handler));

        // Send the message on the shell channel (all these requests go on the shell).
        ShellClientChannel shell = this.connection.getShell();
        shell.sendMessage(message);

        return handler;
    }

    // Local helpers

    private ShellReplyHandler<?> getActiveHandlerForId(String id) {
        return this.activeReplyHandlers.get(id);
    }

    private ReplyHandler<?> getReplyContextHandlerFor(Message<?> message) {
        if (!message.hasParentHeader())
            return this.getWildReplyHandler();

        String id = message.getParentHeader().getId();
        ReplyHandler<?> handler = this.getActiveHandlerForId(id);
        return handler == null ? this.getWildReplyHandler() : handler;
    }

    @SuppressWarnings("unchecked")
    private <T extends ReplyType<?>> ReplyHandler<T> getReplyHandlerFor(Message<T> message) {
        // If no parent header then this message is really wild.
        if (!message.hasParentHeader())
            return this.getWildReplyHandler();

        String id = message.getParentHeader().getId();
        ShellReplyHandler<?> handler = this.getActiveHandlerForId(id);

        // If no handler associated with the parent id then the message is wild.
        if (handler == null)
            return this.getWildReplyHandler();

        // If the result is the correct type or an error reply of the correct type then use
        // the handler.
        MessageType<?> expectedType = handler.getExpectedReplyType();
        MessageType<T> type = message.getHeader().getType();
        if (type.equals(expectedType) || type.isErrorFor(expectedType))
            return (ReplyHandler<T>) handler;

        // Else the message was targeted at a handler of the wrong type, we will complete
        // the future (with an error) so that results aren't kept waiting but give the reply
        // to the wild handler.
        handler.getFutureResult().completeExceptionally(
                new JupyterProtocolException("Bad reply type. Expected " + expectedType.getName() + " but received " + type.getName()));
        return this.getWildReplyHandler();
    }

    public void connect(JupyterClientConnection connection) {
        if (this.connection != null)
            throw new IllegalStateException("Client already connected");

        this.connection = connection;

        // IOPub handlers
        connection.setHandler(MessageType.PUBLISH_STREAM, this::handlePublishStream);
        connection.setHandler(MessageType.PUBLISH_DISPLAY_DATA, this::handlePublishDisplayData);
        connection.setHandler(MessageType.PUBLISH_UPDATE_DISPLAY_DATA, this::handlePublishUpdateDisplayData);
        connection.setHandler(MessageType.PUBLISH_EXECUTE_INPUT, this::handlePublishExecuteInput);
        connection.setHandler(MessageType.PUBLISH_EXECUTION_RESULT, this::handlePublishExecuteResult);
        connection.setHandler(MessageType.PUBLISH_ERROR, this::handlePublishError);
        connection.setHandler(MessageType.PUBLISH_STATUS, this::handlePublishStatus);
        connection.setHandler(MessageType.PUBLISH_CLEAR_OUTPUT, this::handlePublishClearOutput);

        // StdIn handlers
        connection.setHandlerWithStdinReplyEnv(MessageType.INPUT_REQUEST, this::handleInputRequest);

        // Shell handlers
        connection.setHandler(MessageType.EXECUTE_REPLY, this::handleExecuteReply);
        connection.setHandler(MessageType.INSPECT_REPLY, this::handleInspectReply);
        connection.setHandler(MessageType.COMPLETE_REPLY, this::handleCompleteReply);
        connection.setHandler(MessageType.HISTORY_REPLY, this::handleHistoryReply);
        connection.setHandler(MessageType.IS_COMPLETE_REPLY, this::handleIsCompleteReply);
        connection.setHandler(MessageType.KERNEL_INFO_REPLY, this::handleKernelInfoReply);
        connection.setHandler(MessageType.SHUTDOWN_REPLY, this::handleShutdownReply);
        connection.setHandler(MessageType.INTERRUPT_REPLY, this::handleInterruptReply);

        connection.setHandler(MessageType.EXECUTE_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.INSPECT_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.COMPLETE_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.HISTORY_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.IS_COMPLETE_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.KERNEL_INFO_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.SHUTDOWN_REPLY.error(), this::handleErrorReply);
        connection.setHandler(MessageType.INTERRUPT_REPLY.error(), this::handleErrorReply);

        // Comm handlers
        CommClient client = new DefaultCommClient(connection.getShell());
        this.commManager.connectTo(client);
        DefaultCommServer commServer = new DefaultCommServer(this.commManager, client);
        connection.setHandler(MessageType.COMM_OPEN_COMMAND, commServer::handleCommOpenCommand);
        connection.setHandler(MessageType.COMM_MSG_COMMAND, commServer::handleCommMsgCommand);
        connection.setHandler(MessageType.COMM_CLOSE_COMMAND, commServer::handleCommCloseCommand);

        // If the kernel dies, notify all the not yet completed futures that the kernel has
        // died and they will never get a result.
        connection.getHeartbeat().onDeath(() -> {
            synchronized (this.activeReplyHandlers) {
                this.activeReplyHandlers.values().forEach(r -> {
                    CompletableFuture<?> futureResult = r.getFutureResult();
                    if (!futureResult.isDone())
                        futureResult.completeExceptionally(new JupyterKernelDiedException());
                });
            }
        });
    }

    @Override
    public void close() {
        if (this.connection == null)
            return;

        try {
            this.connection.close();
        } finally {
            this.connection = null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // IOPub channel handlers
    ///////////////////////////////////////////////////////////////////////////////

    private void handlePublishStream(Message<PublishStream> message) {
        this.getReplyContextHandlerFor(message).handleStreamIO(message);
    }

    private void handlePublishDisplayData(Message<PublishDisplayData> message) {
        this.getReplyContextHandlerFor(message).handleDisplayData(message);
    }

    private void handlePublishUpdateDisplayData(Message<PublishUpdateDisplayData> message) {
        this.getReplyContextHandlerFor(message).handleUpdateDisplayData(message);
    }

    private void handlePublishExecuteInput(Message<PublishExecuteInput> message) {
        this.handleNotifyOfExecutingCode(message.getContent());
    }

    private void handlePublishExecuteResult(Message<PublishExecuteResult> message) {
        this.getReplyContextHandlerFor(message).handleExecuteResult(message);
    }

    private void handlePublishError(Message<PublishError> message) {
        this.getReplyContextHandlerFor(message).handleError(message);
    }

    private void handlePublishStatus(Message<PublishStatus> message) {
        this.getReplyContextHandlerFor(message).handleStatusUpdate(message);
        this.handleKernelStatusChange(message.getContent());
    }

    private void handlePublishClearOutput(Message<PublishClearOutput> message) {
        this.getReplyContextHandlerFor(message).handleClearOutput(message);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Stdin channel handlers
    ///////////////////////////////////////////////////////////////////////////////

    private void handleInputRequest(ReplyEnvironment env, Message<InputRequest> message) {
        InputReply reply = this.getReplyContextHandlerFor(message).provideStdin(message);
        env.reply(reply);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Shell/Control channel handlers
    ///////////////////////////////////////////////////////////////////////////////

    private void handleErrorReply(Message<ErrorReply> message) {
        this.getReplyHandlerFor((Message<? extends ReplyType<?>>) message)
                .onErrorReply(message);
    }

    private void handleExecuteReply(Message<ExecuteReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleInspectReply(Message<InspectReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleCompleteReply(Message<CompleteReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleHistoryReply(Message<HistoryReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleIsCompleteReply(Message<IsCompleteReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    // connect_reply is deprecated

    // Comm handlers are external

    // The canonical client uses this on initial connection to assert connected.
    private void handleKernelInfoReply(Message<KernelInfoReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleShutdownReply(Message<ShutdownReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }

    private void handleInterruptReply(Message<InterruptReply> message) {
        this.getReplyHandlerFor(message).onReply(message);
    }
}

package io.github.spencerpark.jupyter.kernel;

import io.github.spencerpark.jupyter.api.JupyterIO;
import io.github.spencerpark.jupyter.api.JupyterKernel;
import io.github.spencerpark.jupyter.api.ReplacementOptions;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.history.HistoryEntry;
import io.github.spencerpark.jupyter.api.history.HistoryManager;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.comm.CommClient;
import io.github.spencerpark.jupyter.comm.DefaultCommClient;
import io.github.spencerpark.jupyter.comm.DefaultCommServer;
import io.github.spencerpark.jupyter.messages.Header;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.publish.PublishError;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteInput;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteResult;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ZmqKernelConnector {
    protected final AtomicInteger executionCount = new AtomicInteger(1);
    private final BaseKernel kernel;

    public ZmqKernelConnector(BaseKernel kernel, JupyterConnection connection) {
        this.kernel = kernel;
    }

    public void connectKernelTo(JupyterConnection connection) {
        connection.setHandler(MessageType.EXECUTE_REQUEST, this::handleExecuteRequest);
        connection.setHandler(MessageType.INSPECT_REQUEST, this::handleInspectRequest);
        connection.setHandler(MessageType.COMPLETE_REQUEST, this::handleCompleteRequest);
        connection.setHandler(MessageType.HISTORY_REQUEST, this::handleHistoryRequest);
        connection.setHandler(MessageType.IS_COMPLETE_REQUEST, this::handleIsCodeCompeteRequest);
        connection.setHandler(MessageType.KERNEL_INFO_REQUEST, this::handleKernelInfoRequest);
        connection.setHandler(MessageType.SHUTDOWN_REQUEST, this::handleShutdownRequest);
        connection.setHandler(MessageType.INTERRUPT_REQUEST, this::handleInterruptRequest);

        CommClient client = new DefaultCommClient(connection.getIOPub());
        this.kernel.commManager.connectTo(client);
        DefaultCommServer commServer = new DefaultCommServer(this.kernel.commManager, client);

        connection.setHandler(MessageType.COMM_OPEN_COMMAND, commServer::handleCommOpenCommand);
        connection.setHandler(MessageType.COMM_MSG_COMMAND, commServer::handleCommMsgCommand);
        connection.setHandler(MessageType.COMM_CLOSE_COMMAND, commServer::handleCommCloseCommand);
        connection.setHandler(MessageType.COMM_INFO_REQUEST, commServer::handleCommInfoRequest);
    }

    private void replaceOutputStreams(ShellReplyEnvironment env) {
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;
        InputStream oldStdIn = System.in;

        JupyterIO io = this.kernel.io();
        System.setOut(io.out);
        System.setErr(io.err);
        System.setIn(io.in);

        env.defer(() -> {
            System.setOut(oldStdOut);
            System.setErr(oldStdErr);
            System.setIn(oldStdIn);
        });
    }

    private synchronized void handleExecuteRequest(ShellReplyEnvironment env, Message<ExecuteRequest> executeRequestMessage) {
        this.kernel.commManager.pushContext(executeRequestMessage);
        env.defer(() -> this.kernel.commManager.dropContext(executeRequestMessage));

        ExecuteRequest request = executeRequestMessage.getContent();

        int count = this.executionCount.getAndIncrement();
        //KernelTimestamp start = KernelTimestamp.now();

        env.setBusyDeferIdle();

        env.publish(new PublishExecuteInput(request.getCode(), count));

        DefaultJupyterIO io = this.kernel.io;
        if (this.kernel.shouldReplaceStdStreams())
            this.replaceOutputStreams(env);

        io.setEnv(env);
        env.defer(() -> io.retractEnv(env));

        io.setJupyterInEnabled(request.isStdinEnabled());

        try {
            DisplayData out = this.kernel.eval(request.getCode());

            if (out != null) {
                PublishExecuteResult result = new PublishExecuteResult(count, out);
                env.publish(result);
            }

            env.defer().reply(new ExecuteReply(count, Collections.emptyMap()));
        } catch (Exception e) {
            ErrorReply error = ErrorReply.of(e);
            error.setExecutionCount(count);
            env.publish(PublishError.of(e, this.kernel::formatError));
            env.defer().replyError(ExecuteReply.MESSAGE_TYPE.error(), error);
        }
    }

    private void handleInspectRequest(ShellReplyEnvironment env, Message<InspectRequest> inspectRequestMessage) {
        InspectRequest request = inspectRequestMessage.getContent();
        env.setBusyDeferIdle();
        try {
            DisplayData inspection = this.kernel.inspect(request.getCode(), request.getCursorPos(), request.getDetailLevel() > 0);
            env.reply(new InspectReply(inspection != null, DisplayData.emptyIfNull(inspection)));
        } catch (Exception e) {
            env.replyError(InspectReply.MESSAGE_TYPE.error(), ErrorReply.of(e));
        }
    }

    private void handleCompleteRequest(ShellReplyEnvironment env, Message<CompleteRequest> completeRequestMessage) {
        CompleteRequest request = completeRequestMessage.getContent();
        env.setBusyDeferIdle();
        try {
            ReplacementOptions options = this.kernel.complete(request.getCode(), request.getCursorPos());
            if (options == null)
                env.reply(new CompleteReply(Collections.emptyList(), request.getCursorPos(), request.getCursorPos(), Collections.emptyMap()));
            else
                env.reply(new CompleteReply(options.getReplacements(), options.getSourceStart(), options.getSourceEnd(), Collections.emptyMap()));
        } catch (Exception e) {
            env.replyError(CompleteReply.MESSAGE_TYPE.error(), ErrorReply.of(e));
        }
    }

    private void handleHistoryRequest(ShellReplyEnvironment env, Message<HistoryRequest> historyRequestMessage) {
        // If the manager is unset, short circuit and skip this message
        HistoryManager manager = this.kernel.history();
        if (manager == null) return;

        HistoryRequest request = historyRequestMessage.getContent();
        env.setBusyDeferIdle();

        Set<HistoryManager.ResultFlag> flags = EnumSet.noneOf(HistoryManager.ResultFlag.class);
        if (request.includeOutput()) flags.add(HistoryManager.ResultFlag.INCLUDE_OUTPUT);
        if (!request.useRaw()) flags.add(HistoryManager.ResultFlag.TRANSFORMED_INPUT);

        List<HistoryEntry> entries = null;
        switch (request.getAccessType()) {
            case TAIL:
                HistoryRequest.Tail tailRequest = ((HistoryRequest.Tail) request);
                entries = manager.lookupTail(tailRequest.getMaxReturnLength(), flags);
                break;
            case RANGE:
                HistoryRequest.Range rangeRequest = ((HistoryRequest.Range) request);
                entries = manager.lookupRange(rangeRequest.getSessionIndex(), rangeRequest.getStart(), rangeRequest.getStop(), flags);
                break;
            case SEARCH:
                HistoryRequest.Search searchRequest = ((HistoryRequest.Search) request);
                if (searchRequest.filterUnique()) flags.add(HistoryManager.ResultFlag.UNIQUE);
                entries = manager.search(searchRequest.getPattern(), searchRequest.getMaxReturnLength(), flags);
                break;
        }

        if (entries != null)
            env.reply(new HistoryReply(entries));
    }

    private void handleIsCodeCompeteRequest(ShellReplyEnvironment env, Message<IsCompleteRequest> isCompleteRequestMessage) {
        IsCompleteRequest request = isCompleteRequestMessage.getContent();
        env.setBusyDeferIdle();

        String isCompleteResult = this.kernel.isComplete(request.getCode());

        IsCompleteReply reply;
        switch (isCompleteResult) {
            case JupyterKernel.IS_COMPLETE_YES:
                reply = IsCompleteReply.VALID_CODE;
                break;
            case JupyterKernel.IS_COMPLETE_BAD:
                reply = IsCompleteReply.INVALID_CODE;
                break;
            case JupyterKernel.IS_COMPLETE_MAYBE:
                reply = IsCompleteReply.UNKNOWN;
                break;
            default:
                reply = IsCompleteReply.getIncompleteReplyWithIndent(isCompleteResult);
                break;
        }
        env.reply(reply);
    }

    private void handleKernelInfoRequest(ShellReplyEnvironment env, Message<KernelInfoRequest> kernelInfoRequestMessage) {
        env.setBusyDeferIdle();
        env.reply(new KernelInfoReply(
                        Header.PROTOCOL_VERISON,
                        this.kernel.getKernelName(),
                        this.kernel.getKernelVersion(),
                        this.kernel.getLanguageInfo(),
                        this.kernel.getBanner(),
                        this.kernel.getHelpLinks()
                )
        );
    }

    private void handleShutdownRequest(ShellReplyEnvironment env, Message<ShutdownRequest> shutdownRequestMessage) {
        ShutdownRequest request = shutdownRequestMessage.getContent();
        env.setBusyDeferIdle();

        env.defer().reply(request.isRestart() ? ShutdownReply.SHUTDOWN_AND_RESTART : ShutdownReply.SHUTDOWN);

        this.kernel.onShutdown(request.isRestart());

        env.resolveDeferrals(); //Resolve early because of shutdown

        env.markForShutdown();
    }

    private void handleInterruptRequest(ShellReplyEnvironment env, Message<InterruptRequest> interruptRequestMessage) {
        env.setBusyDeferIdle();
        env.defer().reply(new InterruptReply());

        this.kernel.interrupt();
    }
}

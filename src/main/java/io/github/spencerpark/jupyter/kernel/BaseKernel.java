package io.github.spencerpark.jupyter.kernel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterInputStream;
import io.github.spencerpark.jupyter.channels.JupyterOutputStream;
import io.github.spencerpark.jupyter.channels.ShellReplyEnvironment;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;
import io.github.spencerpark.jupyter.messages.Header;
import io.github.spencerpark.jupyter.messages.MIMEBundle;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.publish.PublishError;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteInput;
import io.github.spencerpark.jupyter.messages.publish.PublishExecuteResult;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.*;
import io.github.spencerpark.jupyter.messages.request.*;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class BaseKernel {
    protected final AtomicInteger executionCount = new AtomicInteger(1);
    private static final Map<String, String> KERNEL_META = ((Supplier<Map<String, String>>) () -> {
        Map<String, String> meta = null;

        InputStream metaStream = BaseKernel.class.getResourceAsStream("kernel-metadata.json");
        if (metaStream != null) {
            Reader metaReader = new InputStreamReader(metaStream);
            try {
                meta = new Gson().fromJson(metaReader, new TypeToken<Map<String, String>>() {
                }.getType());
            } finally {
                try {
                    metaReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (meta == null) {
            meta = new HashMap<>(2);
            meta.put("version", "unknown");
            meta.put("project", "unknown");
        }

        return meta;
    }).get();

    private JupyterOutputStream stdOut;
    private JupyterOutputStream stdErr;
    private JupyterInputStream stdIn;
    protected CommManager commManager;

    public BaseKernel() {
        this.stdOut = new JupyterOutputStream(true);
        this.stdErr = new JupyterOutputStream(false);
        this.stdIn = new JupyterInputStream();
    }

    public String getBanner() {
        LanguageInfo info = this.getLanguageInfo();
        return info != null ? info.getName() + " - " + info.getVersion() : "";
    }

    public List<LanguageInfo.Help> getHelpLinks() {
        return null;
    }

    public abstract MIMEBundle eval(String expr) throws Exception;

    /**
     * Inspect the code to get things such as documentation for a function. This is
     * triggered by {@code shift-tab} in the Jupyter notebook which opens a tooltip displaying
     * the returned bundle.
     * <p>
     * This should aim to return docstrings, function signatures, variable types, etc for
     * the value at the cursor position.
     *
     * @param code        the entire code cell to inspect
     * @param at          the character position within the code cell
     * @param extraDetail true if more in depth detail is requested (for example IPython
     *                    includes the function source in addition to the documentation)
     *
     * @return an output bundle for displaying the documentation or null if nothing is found
     *
     * @throws Exception if the code cannot be inspected for some reason (such as it not
     *                   compiling)
     */
    public MIMEBundle inspect(String code, int at, boolean extraDetail) throws Exception {
        return null;
    }

    /**
     * Try to autocomplete code at a user's cursor such as finishing a method call or
     * variable name. This is triggered by {@code tab} in the Jupyter notebook.
     * <p>
     * If a single value is returned the replacement range in the {@code code} is replaced
     * with the return value.
     * <p>
     * If multiple matches are returned, a tooltip with the values in the order they are
     * returned is displayed that can be selected from.
     * <p>
     * If no matches are returned, no replacements are made. Effectively this is a no-op
     * in that case.
     *
     * @param code the entire code cell containing the code to complete
     * @param at   the character position that the completion is requested at
     *
     * @return the replacement options containing a list of replacement texts and a
     *         source range to overwrite with a user selected replacement from the list
     *
     * @throws Exception if code cannot be completed due to code compilation issues, or
     *                   similar. This should not be thrown if not replacements are available but rather just
     *                   an empty replacements returned.
     */
    public ReplacementOptions complete(String code, int at) throws Exception {
        return null;
    }

    protected static final String IS_COMPLETE_YES = "complete";
    protected static final String IS_COMPLETE_BAD = "invalid";
    protected static final String IS_COMPLETE_MAYBE = "unknown";

    /**
     * Check if the code is complete. This gives frontends the tools to provide
     * console environments that hold of executing code in situations such as
     * {@code "for (int i = 0; i < 10; i++)"} and rather let the newline go to
     * the next line for the developer to input the body of the for loop.
     * <p>
     * There are 4 cases to consider:
     * <p>
     * 1. {@link #IS_COMPLETE_MAYBE} is returned by default and is the equivalent
     * of abstaining from answering the request.<br\>
     * 2. {@link #IS_COMPLETE_BAD} should be returned for invalid code that will
     * result in an error when being parsed/compiled.<br\>
     * 3. {@link #IS_COMPLETE_YES} if the code is a complete, well formed, statement
     * and may be executed. <br\>
     * 4. The code is valid but not yet complete (like the for loop example above). In
     * this case a string describing the prefix to start the next line with (such as 4 spaces
     * following the for loop). <br/>
     *
     * @param code the code to analyze
     *
     * @return {@link #IS_COMPLETE_MAYBE}, {@link #IS_COMPLETE_BAD}, {@link #IS_COMPLETE_YES},
     *         or an indent string
     */
    public String isComplete(String code) {
        return IS_COMPLETE_MAYBE;
    }

    public abstract LanguageInfo getLanguageInfo();

    /**
     * Invoked when the kernel is being shutdown. This is invoked before the
     * connection is shutdown so any last minute messages by the concrete
     * kernel get a chance to send.
     *
     * @param isRestarting true if this is a shutdown will soon be followed
     *                     by a restart. If running in a container or some other
     *                     spawned vm it may be beneficial to keep it alive for a
     *                     bit longer as the kernel is likely to be started up
     *                     again.
     */
    public void onShutdown(boolean isRestarting) {
        //no-op
    }

    /*
     * ===================================
     * | Default handler implementations |
     * ===================================
     */

    public void becomeHandlerForConnection(JupyterConnection connection) {
        connection.setHandler(MessageType.EXECUTE_REQUEST, this::handleExecuteRequest);
        connection.setHandler(MessageType.INSPECT_REQUEST, this::handleInspectRequest);
        connection.setHandler(MessageType.COMPLETE_REQUEST, this::handleCompleteRequest);
        connection.setHandler(MessageType.HISTORY_REQUEST, this::handleHistoryRequest);
        connection.setHandler(MessageType.IS_COMPLETE_REQUEST, this::handleIsCodeCompeteRequest);
        connection.setHandler(MessageType.KERNEL_INFO_REQUEST, this::handleKernelInfoRequest);
        connection.setHandler(MessageType.SHUTDOWN_REQUEST, this::handleShutdownRequest);

        if (this.commManager != null)
            this.commManager.setIOPubChannel(connection.getIOPub());
        else
            this.commManager = new CommManager(connection.getIOPub());
        connection.setHandler(MessageType.COMM_OPEN_COMMAND, commManager::handleCommOpenCommand);
        connection.setHandler(MessageType.COMM_MSG_COMMAND, commManager::handleCommMsgCommand);
        connection.setHandler(MessageType.COMM_CLOSE_COMMAND, commManager::handleCommCloseCommand);
        connection.setHandler(MessageType.COMM_INFO_REQUEST, commManager::handleCommInfoRequest);
    }

    private synchronized void handleExecuteRequest(ShellReplyEnvironment env, Message<ExecuteRequest> executeRequestMessage) {
        this.commManager.setMessageContext(executeRequestMessage);

        ExecuteRequest request = executeRequestMessage.getContent();

        int count = executionCount.getAndIncrement();
        //KernelTimestamp start = KernelTimestamp.now();

        env.setBusyDeferIdle();

        env.publish(new PublishExecuteInput(request.getCode(), count));

        //Intercept the output streams
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;

        this.stdOut.setEnv(env);
        env.defer(() -> {
            System.setOut(oldStdOut);
            this.stdOut.retractEnv(env);
        });
        this.stdErr.setEnv(env);
        env.defer(() -> {
            System.setErr(oldStdErr);
            this.stdErr.retractEnv(env);
        });

        System.setOut(new PrintStream(this.stdOut, true));
        System.setErr(new PrintStream(this.stdErr, true));

        InputStream oldStdIn = System.in;
        this.stdIn.setEnv(env);
        this.stdIn.setEnabled(request.isStdinEnabled());
        System.setIn(this.stdIn);
        //TODO implement Console and pass in a non intrusive way to eval
        //The regular input stream doesn't take advantage of the prompt or password
        //options that the client supports
        env.defer(() -> {
            System.setIn(oldStdIn);
            this.stdIn.retractEnv(env);
        });

        try {
            MIMEBundle out = eval(request.getCode());

            if (out != null) {
                PublishExecuteResult result = new PublishExecuteResult(count, out, new HashMap<>());
                env.publish(result);
            }

            /*Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("dependencies_met", true);
            metadata.put("engine", context.getSessionID());
            metadata.put("status", "ok");
            metadata.put("started", start);*/

            env.defer().reply(new ExecuteReply(count, Collections.emptyMap()));
        } catch (Exception e) {
            ErrorReply error = ErrorReply.of(e);
            error.setExecutionCount(count);
            env.publish(PublishError.of(e));
            env.defer().replyError(ExecuteReply.MESSAGE_TYPE.error(), error);
        }
    }

    private void handleInspectRequest(ShellReplyEnvironment env, Message<InspectRequest> inspectRequestMessage) {
        InspectRequest request = inspectRequestMessage.getContent();
        env.setBusyDeferIdle();
        try {
            MIMEBundle inspection = this.inspect(request.getCode(), request.getCursorPos(), request.getDetailLevel() > 0);
            boolean found = inspection != null && !inspection.isEmpty();
            env.reply(new InspectReply(found, MIMEBundle.emptyIfNull(inspection), Collections.emptyMap()));
        } catch (Exception e) {
            env.replyError(InspectReply.MESSAGE_TYPE.error(), ErrorReply.of(e));
        }
    }

    private void handleCompleteRequest(ShellReplyEnvironment env, Message<CompleteRequest> completeRequestMessage) {
        CompleteRequest request = completeRequestMessage.getContent();
        env.setBusyDeferIdle();
        try {
            ReplacementOptions options = this.complete(request.getCode(), request.getCursorPos());
            if (options == null)
                env.reply(new CompleteReply(Collections.emptyList(), request.getCursorPos(), request.getCursorPos(), Collections.emptyMap()));
            else
                env.reply(new CompleteReply(options.getReplacements(), options.getSourceStart(), options.getSourceEnd(), Collections.emptyMap()));
        } catch (Exception e) {
            env.replyError(CompleteReply.MESSAGE_TYPE.error(), ErrorReply.of(e));
        }
    }

    private void handleHistoryRequest(ShellReplyEnvironment env, Message<HistoryRequest> historyRequestMessage) {
        //Only the qt console uses this one and it only uses the tail search to get where the
        //user left off. Implementing this is not worth the storage overhead as it rarely gets used
        //and in the event that the front end may use it everything still functions fine without it.
    }

    private void handleIsCodeCompeteRequest(ShellReplyEnvironment env, Message<IsCompleteRequest> isCompleteRequestMessage) {
        IsCompleteRequest request = isCompleteRequestMessage.getContent();
        env.setBusyDeferIdle();

        String isCompleteResult = this.isComplete(request.getCode());

        IsCompleteReply reply;
        switch (isCompleteResult) {
            case IS_COMPLETE_YES:
                reply = IsCompleteReply.VALID_CODE;
                break;
            case IS_COMPLETE_BAD:
                reply = IsCompleteReply.INVALID_CODE;
                break;
            case IS_COMPLETE_MAYBE:
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
                        KERNEL_META.get("project"),
                        KERNEL_META.get("version"),
                        this.getLanguageInfo(),
                        this.getBanner(),
                        this.getHelpLinks()
                )
        );
    }

    private void handleShutdownRequest(ShellReplyEnvironment env, Message<ShutdownRequest> shutdownRequestMessage) {
        ShutdownRequest request = shutdownRequestMessage.getContent();
        env.setBusyDeferIdle();

        env.defer().reply(request.isRestart() ? ShutdownReply.SHUTDOWN_AND_RESTART : ShutdownReply.SHUTDOWN);

        this.onShutdown(request.isRestart());

        env.resolveDeferrals(); //Resolve early because of shutdown

        env.markForShutdown();
    }
}

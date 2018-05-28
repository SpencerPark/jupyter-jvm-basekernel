package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageContext;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.publish.PublishStream;

import java.util.Deque;
import java.util.LinkedList;

public class ShellReplyEnvironment {
    private final ShellChannel shell;
    private final StdinChannel stdin;
    private final JupyterSocket iopub;
    private final MessageContext context;

    private Deque<Runnable> deferred = new LinkedList<>();
    private boolean defer = false;

    private boolean requestShutdown = false;

    protected ShellReplyEnvironment(ShellChannel shell, StdinChannel stdin, JupyterSocket iopub, MessageContext context) {
        this.shell = shell;
        this.stdin = stdin;
        this.iopub = iopub;
        this.context = context;
    }

    public void publish(Message<?> msg) {
        if (defer) {
            deferred.push(() -> iopub.sendMessage(msg));
            this.defer = false;
        } else {
            iopub.sendMessage(msg);
        }
    }

    public void reply(Message<?> msg) {
        if (defer) {
            deferred.push(() -> shell.sendMessage(msg));
            this.defer = false;
        } else {
            shell.sendMessage(msg);
        }
    }

    /**
     * Defer the next message send until {@link #resolveDeferrals()}. Deferrals
     * are resolve in a Last In First Out (LIFO) order.
     * <p>
     * The use case that inspired this functionality is the busy-idle protocol
     * component required by Jupyter.
     *
     * <pre>
     *      ShellReplyEnvironment env = ...;
     *
     *      env.setStatusBusy();
     *      env.defer().setStatusIdle(); //Push idle message to defer stack
     *
     *      env.defer().reply(new ExecuteReply(...)); //Push reply to stack
     *
     *      env.writeToStdOut("Test"); //Write "Test" to std out now
     *
     *      env.resolveDeferrals();
     *      //Send the reply
     *      //Send the idle message
     * </pre>
     *
     * @return this instance for call chaining
     */
    public ShellReplyEnvironment defer() {
        this.defer = true;
        return this;
    }

    /**
     * Defer an arbitrary action. See {@link #defer()} but instead of
     * deferring the next message send, defer a specific action.
     *
     * @param action the action to run when the deferrals are resolved
     */
    public void defer(Runnable action) {
        this.deferred.push(action);
    }

    public void resolveDeferrals() {
        if (this.defer)
            throw new IllegalStateException("Reply environment is in defer mode but a resolution was request.");

        while (!deferred.isEmpty())
            deferred.pop().run();
    }

    public void markForShutdown() {
        this.requestShutdown = true;
    }

    public boolean isMarkedForShutdown() {
        return this.requestShutdown;
    }

    public <T extends ContentType<T>> void publish(T content) {
        publish(new Message<>(context, content.getType(), content));
    }

    public <T extends ContentType<T>> void reply(T content) {
        reply(new Message<>(context, content.getType(), content));
    }

    public void replyError(MessageType<?> type, ErrorReply error) {
        reply(new Message(context, type, error));
    }

    public void setStatusBusy() {
        publish(PublishStatus.BUSY);
    }

    public void setStatusIdle() {
        publish(PublishStatus.IDLE);
    }

    public void setBusyDeferIdle() {
        setStatusBusy();
        defer().setStatusIdle();
    }

    public void writeToStdOut(String msg) {
        publish(new PublishStream(PublishStream.StreamType.OUT, msg));
    }

    public void writeToStdErr(String msg) {
        publish(new PublishStream(PublishStream.StreamType.ERR, msg));
    }

    public String readFromStdIn(String prompt, boolean isPassword) {
        return this.stdin.getInput(context, prompt, isPassword);
    }

    public String readFromStdIn(String prompt) {
        return this.readFromStdIn(prompt, false);
    }

    public String readFromStdIn() {
        return this.readFromStdIn("", false);
    }
}

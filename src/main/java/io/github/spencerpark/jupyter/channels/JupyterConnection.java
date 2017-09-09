package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.MessageContext;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import io.github.spencerpark.jupyter.messages.MessageType;
import org.zeromq.ZMQ;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JupyterConnection {
    private final ZMQ.Context ctx;

    protected final HeartbeatChannel heartbeat;
    protected final ShellChannel shell;
    protected final ShellChannel control;
    protected final StdinChannel stdin;
    protected final IOPubChannel iopub;

    private final Map<MessageType, ShellHandler> handlers;

    public JupyterConnection(KernelConnectionProperties connProps) throws NoSuchAlgorithmException, InvalidKeyException {
        this.ctx = ZMQ.context(1);

        HMACGenerator hmacGenerator = connProps.createHMACGenerator();

        this.heartbeat = new HeartbeatChannel(this.ctx, hmacGenerator);
        this.shell = new ShellChannel(this.ctx, hmacGenerator, false, this);
        this.control = new ShellChannel(this.ctx, hmacGenerator, true, this);
        this.stdin = new StdinChannel(this.ctx, hmacGenerator);
        this.iopub = new IOPubChannel(this.ctx, hmacGenerator);

        this.handlers = new HashMap<>();

        forEachSocket(s -> s.bind(connProps));
    }

    public IOPubChannel getIOPub() {
        return this.iopub;
    }

    public <T> void setHandler(MessageType<T> type, ShellHandler<T> handler) {
        this.handlers.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> ShellHandler<T> getHandler(MessageType<T> type) {
        return this.handlers.get(type);
    }

    public ShellReplyEnvironment prepareReplyEnv(ShellChannel shell, MessageContext context) {
        return new ShellReplyEnvironment(shell, this.stdin, this.iopub, context);
    }

    private void forEachSocket(Consumer<JupyterSocket> consumer) {
        consumer.accept(this.heartbeat);
        consumer.accept(this.shell);
        consumer.accept(this.control);
        consumer.accept(this.stdin);
        consumer.accept(this.iopub);
    }

    public void close() {
        forEachSocket(JupyterSocket::close);
        this.ctx.close();
    }

    public void waitUntilClose() {
        forEachSocket(JupyterSocket::waitUntilClose);
    }
}

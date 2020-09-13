package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.channels.DefaultReplyEnvironment;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.channels.ReplyEnvironment;
import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import org.zeromq.ZMQ;

import java.io.Closeable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JupyterClientConnection implements Closeable {
    private final KernelConnectionProperties connProps;

    private boolean isConnected = false;
    private final ZMQ.Context ctx;

    private final Map<MessageType<?>, MessageHandler<?>> handlers;

    protected final HeartbeatClientChannel heartbeat;
    protected final ShellClientChannel shell;
    protected final ShellClientChannel control;
    protected final StdinClientChannel stdin;
    protected final IOPubClientChannel iopub;

    public JupyterClientConnection(KernelConnectionProperties connProps) throws NoSuchAlgorithmException, InvalidKeyException {
        this.connProps = connProps;
        this.ctx = ZMQ.context(1);

        HMACGenerator hmacGenerator = HMACGenerator.fromConnectionProps(connProps);

        this.heartbeat = new HeartbeatClientChannel(this.ctx, hmacGenerator);
        this.shell = new ShellClientChannel(this.ctx, hmacGenerator, false, this);
        this.control = new ShellClientChannel(this.ctx, hmacGenerator, true, this);
        this.stdin = new StdinClientChannel(this.ctx, hmacGenerator, this);
        this.iopub = new IOPubClientChannel(this.ctx, hmacGenerator, this);

        this.handlers = new HashMap<>();
    }

    public void connect() {
        if (!isConnected) {
            forEachSocket(s -> s.bind(this.connProps));
            this.isConnected = true;
        }
    }

    public HeartbeatClientChannel getHeartbeat() {
        return heartbeat;
    }

    public ShellClientChannel getShell() {
        return shell;
    }

    public ShellClientChannel getControl() {
        return control;
    }

    public StdinClientChannel getStdin() {
        return stdin;
    }

    public IOPubClientChannel getIOPub() {
        return iopub;
    }

    public <T> void setHandler(MessageType<T> type, MessageHandler<T> handler) {
        this.handlers.put(type, handler);
    }

    public <T> void setHandlerWithShellReplyEnv(MessageType<T> type, MessageHandler.WithEnvironment<T> handler) {
        this.handlers.put(type, (Message<T> msg) -> {
            ReplyEnvironment env = new DefaultReplyEnvironment(this.getShell(), this.getIOPub(), msg);

            try {
                handler.handle(env, msg);
            } finally {
                env.resolveDeferrals();
            }
        });
    }

    public <T> void setHandlerWithStdinReplyEnv(MessageType<T> type, MessageHandler.WithEnvironment<T> handler) {
        this.handlers.put(type, (Message<T> msg) -> {
            ReplyEnvironment env = new DefaultReplyEnvironment(this.getStdin(), this.getIOPub(), msg);

            try {
                handler.handle(env, msg);
            } finally {
                env.resolveDeferrals();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> MessageHandler<T> getHandler(MessageType<T> type) {
        return (MessageHandler<T>) this.handlers.get(type);
    }

    private void forEachSocket(Consumer<JupyterSocket> consumer) {
        consumer.accept(this.heartbeat);
        consumer.accept(this.shell);
        consumer.accept(this.control);
        consumer.accept(this.stdin);
        consumer.accept(this.iopub);
    }

    @Override
    public void close() {
        forEachSocket(JupyterSocket::close);
    }

    public void waitUntilClose() {
        forEachSocket(JupyterSocket::waitUntilClose);
    }
}

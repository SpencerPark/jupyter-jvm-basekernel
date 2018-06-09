package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShellChannel extends JupyterSocket {
    private static final AtomicInteger SHELL_ID = new AtomicInteger();

    private volatile Loop ioloop;
    private final boolean isControl;
    private final JupyterConnection connection;

    public ShellChannel(ZMQ.Context context, HMACGenerator hmacGenerator, boolean isControl, JupyterConnection connection) {
        super(context, ZMQ.ROUTER, hmacGenerator, Logger.getLogger(isControl ? "ControlChannel" : "ShellChannel"));
        this.isControl = isControl;
        this.connection = connection;
    }

    private boolean isBound() {
        return this.ioloop != null;
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (this.isBound())
            throw new IllegalStateException("Shell channel already bound");

        super.bind(formatAddress(connProps.getTransport(), connProps.getIp(),
                isControl ? connProps.getControlPort() : connProps.getShellPort()));

        ZMQ.Poller poller = super.ctx.poller(1);
        poller.register(this, ZMQ.Poller.POLLIN);

        String channelThreadName = "Shell-" + SHELL_ID.getAndIncrement();
        this.ioloop = new Loop(channelThreadName, 50, () -> {
            int events = poller.poll(0);
            if (events > 0) {
                Message message = super.readMessage();

                ShellHandler handler = connection.getHandler(message.getHeader().getType());
                if (handler != null) {
                    super.logger.info("Handling message: " + message.getHeader().getType().getName());
                    ShellReplyEnvironment env = connection.prepareReplyEnv(this, message);
                    try {
                        handler.handle(env, message);
                    } catch (Exception e) {
                        super.logger.log(Level.SEVERE, "Unhandled exception handling " + message.getHeader().getType().getName() + ". " + e.getClass().getSimpleName() + " - " + e.getLocalizedMessage());
                    } finally {
                        env.resolveDeferrals();
                    }
                    if (env.isMarkedForShutdown())
                        this.connection.close();
                } else {
                    super.logger.log(Level.SEVERE, "Unhandled message: " + message.getHeader().getType().getName());
                }
            }
        });

        this.ioloop.onClose(() -> {
            logger.log(Level.INFO, channelThreadName + " shutdown.");
            this.ioloop = null;
        });

        this.ioloop.start();

        logger.log(Level.INFO, "Polling on " + channelThreadName);
    }

    @Override
    public void close() {
        if (this.isBound())
            this.ioloop.shutdown();

        super.close();
    }

    @Override
    public void waitUntilClose() {
        if (this.ioloop != null) {
            try {
                this.ioloop.join();
            } catch (InterruptedException ignored) { }
        }
    }
}

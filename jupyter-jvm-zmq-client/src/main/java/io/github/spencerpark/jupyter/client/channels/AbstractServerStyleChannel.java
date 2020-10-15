package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.channels.Loop;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractServerStyleChannel extends JupyterSocket {
    protected volatile Loop ioloop;

    protected final JupyterClientConnection connection;
    protected final long sleep;

    public AbstractServerStyleChannel(ZContext context, SocketType zmqType, HMACGenerator hmacGenerator, Logger logger, JupyterClientConnection connection, long sleep) {
        super(context, zmqType, hmacGenerator, logger);
        this.connection = connection;
        this.sleep = sleep;
    }

    protected boolean isBound() {
        return this.ioloop != null;
    }

    @SuppressWarnings("unchecked")
    protected void startServerLoop(String channelThreadName) {
        ZMQ.Poller poller = super.ctx.createPoller(1);
        poller.register(this, ZMQ.Poller.POLLIN);

        this.ioloop = new Loop(channelThreadName, this.sleep, () -> {
            int events = poller.poll(0);
            for (int i = 0; i < events; i++) {
                Message message = super.readMessage();

                MessageType<?> type = message.getHeader().getType();
                MessageHandler handler = this.connection.getHandler(type);

                try {
                    handler.handle(message);
                } catch (Exception e) {
                    super.logger.log(Level.SEVERE, "Unhandled exception while handling " + type.getName() + ". " + e.getClass().getSimpleName() + " - " + e.getLocalizedMessage());
                }
            }
        });

        this.ioloop.onClose(() -> {
            logger.log(Level.INFO, channelThreadName + " shutdown.");
            this.ioloop = null;
        });

        logger.log(Level.INFO, "Polling on " + channelThreadName);
        this.ioloop.start();
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

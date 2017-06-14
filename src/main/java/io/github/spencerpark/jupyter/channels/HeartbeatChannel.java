package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatChannel extends JupyterSocket {
    private static final AtomicInteger HEARTBEAT_ID = new AtomicInteger();

    private volatile Loop pulse;

    public HeartbeatChannel(ZMQ.Context context, HMACGenerator hmacGenerator) {
        super(context, ZMQ.REP, hmacGenerator, Logger.getLogger("HeartbeatChannel"));
    }

    private boolean isBound() {
        return this.pulse != null;
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (this.isBound())
            throw new IllegalStateException("Heartbeat channel already bound");

        super.bind(JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getHbPort()));

        ZMQ.Poller poller = super.ctx.poller(1);
        poller.register(this, ZMQ.Poller.POLLIN);

        this.pulse = new Loop("Heartbeat-" + HEARTBEAT_ID.getAndIncrement(), 500, () -> {
            int events = poller.poll(0);
            if (events > 0) {
                byte[] msg = this.recv();
                if (msg == null) {
                    //Error during receive, just continue
                    super.logger.log(Level.SEVERE, "Poll returned 1 event but could not read the echo string");
                    return;
                }
                if (!this.send(msg)) {
                    super.logger.log(Level.SEVERE, "Could not send heartbeat reply");
                }
                super.logger.log(Level.FINEST, "Heartbeat pulse");
            }
        });
        this.pulse.onClose(() -> {
            logger.log(Level.INFO, this.pulse.getName() + " shutdown.");
            this.pulse = null;
        });
        this.pulse.start();
        logger.log(Level.INFO, "Polling on " + this.pulse.getName());
    }

    @Override
    public void close() {
        if (this.isBound())
            this.pulse.shutdown();

        super.close();
    }

    @Override
    public void waitUntilClose() {
        if (this.pulse != null) {
            try {
                this.pulse.join();
            } catch (InterruptedException ignored) { }
        }
    }
}

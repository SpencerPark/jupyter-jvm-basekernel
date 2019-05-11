package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StdinClientChannel extends AbstractServerStyleChannel {
    private static final long DEFAULT_LOOP_SLEEP_MS = 50;
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger();

    public StdinClientChannel(ZMQ.Context context, HMACGenerator hmacGenerator, JupyterClientConnection connection) {
        this(context, hmacGenerator, connection, DEFAULT_LOOP_SLEEP_MS);
    }

    public StdinClientChannel(ZMQ.Context context, HMACGenerator hmacGenerator, JupyterClientConnection connection, long sleep) {
        super(context, SocketType.DEALER, hmacGenerator, Logger.getLogger("StdinChannel-client"), connection, sleep);
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (super.isBound())
            throw new IllegalStateException("Stdin client channel is already bound.");

        String addr = JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getStdinPort());
        String channelThreadName = "Stdin-client-" + NEXT_INSTANCE_ID.getAndIncrement();

        logger.log(Level.INFO, String.format("Binding %s to %s.", channelThreadName, addr));
        super.connect(addr);

        super.startServerLoop(channelThreadName);
    }
}

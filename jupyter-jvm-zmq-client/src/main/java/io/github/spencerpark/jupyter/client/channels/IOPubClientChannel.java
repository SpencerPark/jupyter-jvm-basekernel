package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOPubClientChannel extends AbstractServerStyleChannel {
    private static final long DEFAULT_LOOP_SLEEP_MS = 50;
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger();

    public IOPubClientChannel(ZContext context, HMACGenerator hmacGenerator, JupyterClientConnection connection) {
        this(context, hmacGenerator, connection, DEFAULT_LOOP_SLEEP_MS);
    }

    public IOPubClientChannel(ZContext context, HMACGenerator hmacGenerator, JupyterClientConnection connection, long sleep) {
        super(context, SocketType.SUB, hmacGenerator, Logger.getLogger("IOPubChannel-client"), connection, sleep);
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (super.isBound())
            throw new IllegalStateException("IOPub client channel is already bound.");

        String addr = JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getIopubPort());
        String channelThreadName = "IOPub-client-" + NEXT_INSTANCE_ID.getAndIncrement();

        logger.log(Level.INFO, String.format("Binding %s to %s.", channelThreadName, addr));
        super.connect(addr);
        super.subscribe(new byte[]{}); // A sub needs to subscribe to a topic. The jupyter client subs to b'' so we do the same.

        super.startServerLoop(channelThreadName);
    }
}

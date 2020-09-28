package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShellClientChannel extends AbstractServerStyleChannel {
    private static final long DEFAULT_LOOP_SLEEP_MS = 50;
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger();

    private final boolean isControl;

    public ShellClientChannel(ZContext context, HMACGenerator hmacGenerator, boolean isControl, JupyterClientConnection connection) {
        this(context, hmacGenerator, isControl, connection, DEFAULT_LOOP_SLEEP_MS);
    }

    public ShellClientChannel(ZContext context, HMACGenerator hmacGenerator, boolean isControl, JupyterClientConnection connection, long sleep) {
        super(context, SocketType.DEALER, hmacGenerator, Logger.getLogger(isControl ? "ControlChannel-client" : "ShellChannel-client"), connection, sleep);
        this.isControl = isControl;
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (this.isBound())
            throw new IllegalStateException("Shell channel already bound");

        String channelThreadName = "Shell-client-" + NEXT_INSTANCE_ID.getAndIncrement();
        String addr = JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(),
                isControl ? connProps.getControlPort() : connProps.getShellPort());

        logger.log(Level.INFO, String.format("Binding %s to %s.", channelThreadName, addr));
        super.connect(addr);

        super.startServerLoop(channelThreadName);
    }
}

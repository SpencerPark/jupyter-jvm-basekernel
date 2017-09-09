package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.ZMQ;

import java.util.logging.Logger;

public class IOPubChannel extends JupyterSocket {
    public IOPubChannel(ZMQ.Context context, HMACGenerator hmacGenerator) {
        super(context, ZMQ.PUB, hmacGenerator, Logger.getLogger("IOPubChannel"));
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        super.bind(JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getIopubPort()));
    }
}

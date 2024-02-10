package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class IOPubChannel extends JupyterSocket {
    private static final Logger LOG = LoggerFactory.getLogger(IOPubChannel.class);

    public IOPubChannel(ZMQ.Context context, HMACGenerator hmacGenerator) {
        super(context, SocketType.PUB, hmacGenerator);
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        String addr = JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getIopubPort());

        LOG.info("Binding iopub to {}.", addr);
        super.bind(addr);
    }
}

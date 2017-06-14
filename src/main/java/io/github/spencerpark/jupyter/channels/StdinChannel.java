package io.github.spencerpark.jupyter.channels;

import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageContext;
import io.github.spencerpark.jupyter.messages.reply.InputReply;
import io.github.spencerpark.jupyter.messages.request.InputRequest;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.ZMQ;

import java.util.logging.Logger;

public class StdinChannel extends JupyterSocket {
    public StdinChannel(ZMQ.Context context, HMACGenerator hmacGenerator) {
        super(context, ZMQ.ROUTER, hmacGenerator, Logger.getLogger("StdinChannel"));
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        super.bind(formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getStdinPort()));
    }

    /**
     * Ask the frontend for input.
     * <p>
     * <strong>Do not ask for input if an execute request has `allow_stdin=False`</strong>
     *
     * @param context           a message that the request with input was invoked by such as an execute request
     * @param prompt            a prompt string for the front end to include with the input request
     * @param isPasswordRequest a flag specifying if the input request is for a password, if so
     *                          the frontend should obscure the user input (for example with password
     *                          dots or not echoing the input)
     *
     * @return the input string from the frontend.
     */
    public synchronized String getInput(MessageContext context, String prompt, boolean isPasswordRequest) {
        InputRequest content = new InputRequest(prompt, isPasswordRequest);
        Message<InputRequest> request = new Message<>(context, InputRequest.MESSAGE_TYPE, content);

        super.sendMessage(request);

        Message<InputReply> reply = super.readMessage(InputReply.MESSAGE_TYPE);

        return reply.getContent().getValue() + System.lineSeparator();
    }
}

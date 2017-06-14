package io.github.spencerpark.jupyter.channels;

import com.google.gson.*;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.*;
import io.github.spencerpark.jupyter.messages.adapters.*;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class JupyterSocket extends ZMQ.Socket {
    protected static String formatAddress(String transport, String ip, int port) {
        return transport + "://" + ip + ":" + Integer.toString(port);
    }

    public static JupyterSocket makeIopub(ZMQ.Context ctx, HMACGenerator generator) {
        return new JupyterSocket(ctx, ZMQ.PUB, generator, Logger.getLogger("IOPubChannel")) {
            @Override
            public void bind(KernelConnectionProperties connProps) {
                super.bind(formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getIopubPort()));
            }
        };
    }

    private static final byte[] IDENTITY_BLOB_DELIMITER = "<IDS|MSG>".getBytes();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(KernelTimestamp.class, KernelTimestampAdapter.INSTANCE)
            .registerTypeAdapter(Header.class, HeaderAdapter.INSTANCE)
            .registerTypeAdapter(MessageType.class, MessageTypeAdapter.INSTANCE)
            .registerTypeAdapter(PublishStatus.class, PublishStatusAdapter.INSTANCE)
            .registerTypeAdapter(MIMEBundle.class, MIMEBundleAdapter.INSTANCE)
            //.setPrettyPrinting()
            .create();
    private static final JsonParser json = new JsonParser();
    private static final byte[] EMPTY_JSON_OBJECT = "{}".getBytes();

    protected final ZMQ.Context ctx;
    protected final HMACGenerator hmacGenerator;
    protected final Logger logger;
    protected boolean closed;

    protected JupyterSocket(ZMQ.Context context, int type, HMACGenerator hmacGenerator, Logger logger) {
        super(context, type);
        this.ctx = context;
        this.hmacGenerator = hmacGenerator;
        this.logger = logger;
        this.closed = false;
    }

    public abstract void bind(KernelConnectionProperties connProps);

    //TODO fromJson is what takes the longest
    public synchronized Message<?> readMessage() {
        if (this.closed)
            return null;

        List<byte[]> identities = new LinkedList<>();
        byte[] identity = super.recv();
        while (!Arrays.equals(IDENTITY_BLOB_DELIMITER, identity)) {
            identities.add(identity);
            identity = super.recv();
        }

        //A hex string
        String receivedSig = super.recvStr();

        byte[] headerRaw = super.recv();
        byte[] parentHeaderRaw = super.recv();
        byte[] metadataRaw = super.recv();
        byte[] contentRaw = super.recv();

        List<byte[]> blobs = new LinkedList<>();
        while (super.hasReceiveMore()) blobs.add(super.recv());

        String calculatedSig = this.hmacGenerator.calculateSignature(headerRaw, parentHeaderRaw, metadataRaw, contentRaw);

        if (calculatedSig != null && !calculatedSig.equals(receivedSig))
            throw new SecurityException("Message received had invalid signature");

        Header<?> header = gson.fromJson(new String(headerRaw), Header.class);

        Header<?> parentHeader = null;
        JsonElement parentHeaderJson = json.parse(new String(parentHeaderRaw));
        if (parentHeaderJson.isJsonObject() && parentHeaderJson.getAsJsonObject().size() > 0)
            parentHeader = gson.fromJson(parentHeaderJson, Header.class);

        JsonObject metadata = json.parse(new String(metadataRaw)).getAsJsonObject();
        Object content = gson.fromJson(new String(contentRaw), header.getType().getContentType());

        Message message = new Message(identities, header, parentHeader, metadata, content, blobs);

        logger.finer(() -> "Received from " + super.base().getsockoptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));

        return message;
    }

    //This doesn't necessarily need to be marked synchronized but the underlying call
    //to readMessage() is and this makes it clearer that the bulk of the method is synchronized
    public synchronized <T> Message<T> readMessage(MessageType<T> type) {
        Message<?> message = readMessage();
        if (message.getHeader().getType() != type) {
            throw new RuntimeException("Expected a " + type + " message but received a " + message.getHeader().getType() + " message.");
        }
        return (Message<T>) message;
    }

    public synchronized void sendMessage(Message<?> message) {
        if (this.closed)
            return;

        byte[] headerRaw = gson.toJson(message.getHeader()).getBytes();
        byte[] parentHeaderRaw = message.hasParentHeader()
                ? gson.toJson(message.getParentHeader()).getBytes()
                : EMPTY_JSON_OBJECT;
        byte[] metadata = message.hasMetadata()
                ? gson.toJson(message.getMetadata()).getBytes()
                : EMPTY_JSON_OBJECT;
        byte[] content = gson.toJson(message.getContent()).getBytes();

        String hmac = hmacGenerator.calculateSignature(headerRaw, parentHeaderRaw, metadata, content);

        logger.finer(() -> "Sending to " + super.base().getsockoptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));

        message.getIdentities().forEach(super::sendMore);
        super.sendMore(IDENTITY_BLOB_DELIMITER);
        super.sendMore(hmac.getBytes());
        super.sendMore(headerRaw);
        super.sendMore(parentHeaderRaw);
        super.sendMore(metadata);

        if (message.getBlobs() == null)
            super.send(content);
        else {
            super.sendMore(content);
            //The last call needs to be a "send" call so as long as "blobs.hasNext()"
            //there will be something sent later and so the call needs to be "sendMore"
            Iterator<byte[]> blobs = message.getBlobs().iterator();
            byte[] blob;
            while (blobs.hasNext()) {
                blob = blobs.next();
                if (blobs.hasNext())
                    super.sendMore(blob);
                else
                    super.send(blob);
            }
        }
    }

    @Override
    public void close() {
        super.close();
        this.closed = true;
    }

    public void waitUntilClose() { }
}

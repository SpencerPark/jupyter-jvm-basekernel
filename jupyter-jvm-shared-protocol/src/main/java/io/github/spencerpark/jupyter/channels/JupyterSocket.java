package io.github.spencerpark.jupyter.channels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.api.ExpressionValue;
import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.api.history.HistoryEntry;
import io.github.spencerpark.jupyter.messages.*;
import io.github.spencerpark.jupyter.messages.adapters.*;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.request.HistoryRequest;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

public abstract class JupyterSocket extends ZMQ.Socket {
    protected static String formatAddress(String transport, String ip, int port) {
        return transport + "://" + ip + ":" + Integer.toString(port);
    }

    public static final Charset ASCII = Charset.forName("ascii");
    public static final Charset UTF_8 = Charset.forName("utf8");

    private static final byte[] IDENTITY_BLOB_DELIMITER = "<IDS|MSG>".getBytes(ASCII); // Comes from a python bytestring
    private static final Gson replyGson = new GsonBuilder()
            .registerTypeAdapter(HistoryEntry.class, HistoryEntryAdapter.INSTANCE)
            .registerTypeAdapter(ExpressionValue.class, ExpressionValueAdapter.INSTANCE)
            .create();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(KernelTimestamp.class, KernelTimestampAdapter.INSTANCE)
            .registerTypeAdapter(Header.class, HeaderAdapter.INSTANCE)
            .registerTypeAdapter(MessageType.class, MessageTypeAdapter.INSTANCE)
            .registerTypeAdapter(PublishStatus.class, PublishStatusAdapter.INSTANCE)
            .registerTypeAdapter(HistoryRequest.class, HistoryRequestAdapter.INSTANCE)
            .registerTypeHierarchyAdapter(ReplyType.class, new ReplyTypeAdapter(replyGson))
            //.setPrettyPrinting()
            .create();
    private static final byte[] EMPTY_JSON_OBJECT = "{}".getBytes(UTF_8);
    private static final Type JSON_OBJ_AS_MAP = new TypeToken<Map<String, Object>>() {
    }.getType();

    public static final Logger JUPYTER_LOGGER = Logger.getLogger("Jupyter");

    protected final ZContext ctx;
    protected final HMACGenerator hmacGenerator;
    protected final Logger logger;
    protected boolean closed;

    protected JupyterSocket(ZContext context, SocketType type, HMACGenerator hmacGenerator, Logger logger) {
        super(context.getContext(), type);
        this.ctx = context;
        this.hmacGenerator = hmacGenerator;
        logger.setParent(JUPYTER_LOGGER);
        this.logger = logger;
        this.closed = false;
    }

    public abstract void bind(KernelConnectionProperties connProps);

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

        Header<?> header = gson.fromJson(new String(headerRaw, UTF_8), Header.class);

        Header<?> parentHeader = null;
        JsonElement parentHeaderJson = JsonParser.parseString(new String(parentHeaderRaw, UTF_8));
        if (parentHeaderJson.isJsonObject() && parentHeaderJson.getAsJsonObject().size() > 0)
            parentHeader = gson.fromJson(parentHeaderJson, Header.class);

        Map<String, Object> metadata = gson.fromJson(new String(metadataRaw, UTF_8), JSON_OBJ_AS_MAP);
        // The cast to type is very important, if a class is given, gson will attempt to cast the result. In the case
        // of an error reply we don't return an instance of the class but rather an `ErrorReply`. The code here handles
        // that case gracefully.
        Object content = gson.fromJson(new String(contentRaw, UTF_8), (Type) header.getType().getContentType());
        if (content instanceof ErrorReply)
            header = new Header<>(header.getId(), header.getUsername(), header.getSessionId(), header.getTimestamp(), header.getType().error(), header.getVersion());

        @SuppressWarnings("unchecked")
        Message<?> message = new Message(identities, header, parentHeader, metadata, content, blobs);

        logger.finer(() -> "Received from " + super.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));

        return message;
    }

    @SuppressWarnings("unchecked")
    public <T> Message<T> readMessage(MessageType<T> type) {
        Message<?> message = readMessage();
        if (message.getHeader().getType() != type) {
            throw new RuntimeException("Expected a " + type + " message but received a " + message.getHeader().getType() + " message.");
        }
        return (Message<T>) message;
    }

    public synchronized void sendMessage(Message<?> message) {
        if (this.closed)
            return;

        byte[] headerRaw = gson.toJson(message.getHeader()).getBytes(UTF_8);
        byte[] parentHeaderRaw = message.hasParentHeader()
                ? gson.toJson(message.getParentHeader()).getBytes(UTF_8)
                : EMPTY_JSON_OBJECT;
        byte[] metadata = message.hasMetadata()
                ? gson.toJson(message.getMetadata()).getBytes(UTF_8)
                : EMPTY_JSON_OBJECT;
        byte[] content = gson.toJson(message.getContent()).getBytes(UTF_8);

        String hmac = hmacGenerator.calculateSignature(headerRaw, parentHeaderRaw, metadata, content);

        logger.finer(() -> "Sending to " + super.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));

        message.getIdentities().forEach(super::sendMore);
        super.sendMore(IDENTITY_BLOB_DELIMITER);
        super.sendMore(hmac.getBytes(ASCII));
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

    public void waitUntilClose() {
    }
}

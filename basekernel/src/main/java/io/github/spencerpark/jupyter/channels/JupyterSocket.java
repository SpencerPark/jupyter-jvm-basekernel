package io.github.spencerpark.jupyter.channels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.spencerpark.jupyter.kernel.ExpressionValue;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.history.HistoryEntry;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import io.github.spencerpark.jupyter.messages.Header;
import io.github.spencerpark.jupyter.messages.KernelTimestamp;
import io.github.spencerpark.jupyter.messages.Message;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.adapters.ExpressionValueAdapter;
import io.github.spencerpark.jupyter.messages.adapters.HeaderAdapter;
import io.github.spencerpark.jupyter.messages.adapters.HistoryEntryAdapter;
import io.github.spencerpark.jupyter.messages.adapters.HistoryRequestAdapter;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;
import io.github.spencerpark.jupyter.messages.adapters.KernelTimestampAdapter;
import io.github.spencerpark.jupyter.messages.adapters.MessageTypeAdapter;
import io.github.spencerpark.jupyter.messages.adapters.PublishStatusAdapter;
import io.github.spencerpark.jupyter.messages.adapters.ReplyTypeAdapter;
import io.github.spencerpark.jupyter.messages.publish.PublishStatus;
import io.github.spencerpark.jupyter.messages.reply.ErrorReply;
import io.github.spencerpark.jupyter.messages.request.HistoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class JupyterSocket extends ZMQ.Socket {
    private static final Logger LOG = LoggerFactory.getLogger(JupyterSocket.class);

    protected static String formatAddress(String transport, String ip, int port) {
        return transport + "://" + ip + ":" + Integer.toString(port);
    }

    public static final Charset ASCII = StandardCharsets.US_ASCII;
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final byte[] IDENTITY_BLOB_DELIMITER = "<IDS|MSG>".getBytes(ASCII); // Comes from a python bytestring
    private static final Gson replyGson = JsonBox.registerTypeAdapters(new GsonBuilder())
            .registerTypeAdapter(HistoryEntry.class, HistoryEntryAdapter.INSTANCE)
            .registerTypeAdapter(ExpressionValue.class, ExpressionValueAdapter.INSTANCE)
            .create();
    private static final Gson gson = JsonBox.registerTypeAdapters(new GsonBuilder())
            .registerTypeAdapter(KernelTimestamp.class, KernelTimestampAdapter.INSTANCE)
            .registerTypeAdapter(Header.class, HeaderAdapter.INSTANCE)
            .registerTypeAdapter(MessageType.class, MessageTypeAdapter.INSTANCE)
            .registerTypeAdapter(PublishStatus.class, PublishStatusAdapter.INSTANCE)
            .registerTypeAdapter(HistoryRequest.class, HistoryRequestAdapter.INSTANCE)
            .registerTypeHierarchyAdapter(ReplyType.class, new ReplyTypeAdapter(replyGson))
            //.setPrettyPrinting()
            .create();
    private static final byte[] EMPTY_JSON_OBJECT = "{}".getBytes(UTF_8);
    private static final Type JSON_OBJ_AS_MAP = new TypeToken<Map<String, Object>>() {}.getType();

    protected final ZMQ.Context ctx;
    protected final HMACGenerator hmacGenerator;
    protected boolean closed;

    protected JupyterSocket(ZMQ.Context context, SocketType type, HMACGenerator hmacGenerator) {
        super(context, type);
        this.ctx = context;
        this.hmacGenerator = hmacGenerator;
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
        if (parentHeaderJson.isJsonObject() && !parentHeaderJson.getAsJsonObject().isEmpty())
            parentHeader = gson.fromJson(parentHeaderJson, Header.class);

        Map<String, Object> metadata = gson.fromJson(new String(metadataRaw, UTF_8), JSON_OBJ_AS_MAP);
        Object content = gson.fromJson(new String(contentRaw, UTF_8), header.getType().getContentType());
        if (content instanceof ErrorReply)
            header = new Header<>(header.getId(), header.getUsername(), header.getSessionId(), header.getTimestamp(), header.getType().error(), header.getVersion());

        @SuppressWarnings("unchecked")
        Message<?> message = new Message(identities, header, parentHeader, metadata, content, blobs);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Received from " + super.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));
        }

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

        if (LOG.isTraceEnabled()) {
            LOG.trace("Sending to " + super.base().getSocketOptx(zmq.ZMQ.ZMQ_LAST_ENDPOINT) + ":\n" + gson.toJson(message));
        }

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

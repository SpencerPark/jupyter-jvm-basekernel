package io.github.spencerpark.jupyter.messages;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

public class Message<T> implements MessageContext {
    private List<byte[]> identities;

    private Header<T> header;

    /**
     * Optional, in a chain of messages this is copied from
     * the parent so the client can better track where the messages
     * come from.
     */
    private Header<?> parentHeader;

    private JsonObject metadata;

    private T content;

    private List<byte[]> blobs;

    public Message(MessageContext ctx, MessageType<T> type, T content) {
        this.identities = ctx.getIdentities();
        this.header = new Header<>(ctx, type);
        this.content = content;
        this.parentHeader = ctx.getHeader();
        this.metadata = null;
        this.content = content;
        this.blobs = null;
    }

    public Message(List<byte[]> identities, Header<T> header, T content) {
        this.identities = identities;
        this.header = header;
        this.parentHeader = null;
        this.metadata = null;
        this.content = content;
        this.blobs = null;
    }

    public Message(List<byte[]> identities, Header<T> header, Header<?> parentHeader, JsonObject metadata, T content, List<byte[]> blobs) {
        this.identities = identities;
        this.header = header;
        this.parentHeader = parentHeader;
        this.metadata = metadata;
        this.content = content;
        this.blobs = blobs;
    }

    @Override
    public List<byte[]> getIdentities() {
        return identities;
    }

    @Override
    public Header<T> getHeader() {
        return header;
    }

    public boolean hasParentHeader() {
        return parentHeader != null;
    }

    public Header<?> getParentHeader() {
        return parentHeader;
    }

    public boolean hasMetadata() {
        return metadata != null;
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    public T getContent() {
        return content;
    }

    public List<byte[]> getBlobs() {
        return blobs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Message {\n");
        sb.append("\tidentities = [\n");
        for (byte[] id : identities)
            sb.append("\t\t").append(Arrays.toString(id)).append("\n");
        sb.append("\t]\n");
        sb.append("\theader = ").append(header).append("\n");
        sb.append("\tparentHeader = ").append(parentHeader).append("\n");
        sb.append("\tmetadata = ").append(metadata).append("\n");
        sb.append("\tcontent = ").append(content).append("\n");
        sb.append("\tblobs = [\n");
        if (blobs != null)
            for (byte[] blob : blobs)
                sb.append("\t\t").append(Arrays.toString(blob)).append("\n");
        sb.append("\t]\n");
        sb.append("}\n");
        return sb.toString();
    }
}

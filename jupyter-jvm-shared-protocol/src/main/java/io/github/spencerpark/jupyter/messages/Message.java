package io.github.spencerpark.jupyter.messages;

import java.util.*;

public class Message<T> implements MessageContext {
    private List<byte[]> identities;

    private Header<T> header;

    /**
     * Optional, in a chain of messages this is copied from
     * the parent so the client can better track where the messages
     * come from.
     */
    private Header<?> parentHeader;

    private Map<String, Object> metadata;

    private T content;

    private List<byte[]> blobs;

    public Message(MessageContext ctx, MessageType<T> type, T content) {
        this(ctx, type, content, null, null);
    }

    public Message(MessageContext ctx, MessageType<T> type, T content, List<byte[]> blobs, Map<String, Object> metadata) {
        this(
                ctx != null ? ctx.getIdentities() : Collections.emptyList(),
                new Header<>(ctx, type),
                ctx != null ? ctx.getHeader() : null,
                metadata,
                content,
                blobs
        );
    }

    public Message(Header<T> header, T content) {
        this(Collections.emptyList(), header, null, null, content, null);
    }

    public Message(Header<T> header, T content, Map<String, Object> metadata, List<byte[]> blobs) {
        this(Collections.emptyList(), header, null, metadata, content, blobs);
    }

    public Message(List<byte[]> identities, Header<T> header, T content) {
        this(identities, header, null, null, content, null);
    }

    public Message(List<byte[]> identities, Header<T> header, Header<?> parentHeader, Map<String, Object> metadata, T content, List<byte[]> blobs) {
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getNonNullMetadata() {
        if (this.hasMetadata())
            return this.getMetadata();
        this.metadata = new LinkedHashMap<>();
        return this.metadata;
    }

    public T getContent() {
        return content;
    }

    public boolean hasBlobs() {
        return blobs != null;
    }

    public List<byte[]> getBlobs() {
        return blobs;
    }

    public List<byte[]> getNonNullBlobs() {
        if (this.hasBlobs())
            return this.blobs;
        this.blobs = new LinkedList<>();
        return this.blobs;
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

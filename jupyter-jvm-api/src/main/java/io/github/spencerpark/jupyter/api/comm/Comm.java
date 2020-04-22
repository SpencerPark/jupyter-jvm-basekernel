package io.github.spencerpark.jupyter.api.comm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public abstract class Comm {
    private final CommManager manager;
    private final String id;
    private final String targetName;

    private boolean closed = false;

    public Comm(CommManager manager, String id, String targetName) {
        this(manager, id, targetName, null);
    }

    public Comm(CommManager manager, String id, String targetName, JsonElement initializationData) {
        this.manager = manager;
        this.id = id;
        this.targetName = targetName;
    }

    public CommManager getManager() {
        return this.manager;
    }

    public String getID() {
        return this.id;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public boolean isClosed() {
        return closed;
    }

    public void send(JsonObject data) {
        this.manager.messageComm(this.getID(), data);
    }

    public void send(JsonObject data, Map<String, Object> metadata, List<byte[]> blobs) {
        this.manager.messageComm(this.getID(), data, metadata, blobs);
    }

    /**
     * A callback for when the kernel receives a message who's destination is
     * this comm. This handler gets access to the entire message so that if desired
     * the comms may make use of the low level blob segments or metadata.
     * <p>
     * The data that most comms would be interested in is the {@link CommMessage.Data#getData()}
     * which is the payload attached to a message when sent via the frontend's {@code comm.send({})}
     * function. It is recommended to deserialize it this handler to avoid
     * passing the JsonObject to too many other classes in case the serialization library changes in
     * the future.
     *
     * @param message the message received from the frontend that is targeted at this comm
     */
    public abstract void onMessage(CommMessage.Data message);

    /**
     * Invoked when this comm is closed. The similar {@link Comm#close()} method is used
     * to close this comm where as this {@code onClose} is a callback to clean up this comm
     * when it is closed either by this side or as the result of a message from the frontend.
     * <dl>
     * <dt>If {@code sending}:</dt>
     * <dd>
     * then this method is free to modify the {@code closeMessage} to add any additional data
     * to the {@link CommMessage.Close#getData()} or the {@link CommMessage#getBlobs()}. The message
     * will be sent <strong>after</strong> the execution of this method.
     * </dd>
     * <dt>If {@code !sending}:</dt>
     * <dd>
     * then this method may be interested in using the destructuring data in {@link CommMessage.Close#getData()}
     * that is sent by the front-end upon triggering the close.
     * </dd>
     * </dl>
     *
     * @param closeMessage the message triggering the close if from. This may contain some destructuring
     *                     parameters in {@link CommMessage.Close#getData()} if the frontend component
     *                     decided to send something.
     * @param sending      a boolean flag signaling if the close is the being triggered by this side
     *                     ({@code sending == true}) or from the front-end ({@code sending == false}).
     */
    public abstract void onClose(CommMessage.Close closeMessage, boolean sending);

    public final void close(JsonObject msg) {
        if (this.closed) return;
        this.manager.closeComm(this, msg);
    }

    public final void close() {
        if (this.closed) return;
        this.manager.closeComm(this);
        this.closed = true;
    }
}

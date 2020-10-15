package io.github.spencerpark.jupyter.api.comm;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * A CommManager is responsible for keeping track of a group of comms created by any
 * of their registered {@link CommTarget}s.
 */
public interface CommManager extends Iterable<Comm> {
    /**
     * Lookup a comm by its unique id. If the id is unknown
     * to this manager it may return null.
     *
     * @param id the comm id
     *
     * @return the {@link Comm} with the associated id or null if the id is unknown
     */
    public Comm getCommByID(String id);

    /**
     * Open a communication with the frontend. In the event that the front end does
     * not have a target registered with the {@code targetName} the expected behaviour is for
     * it to send a {@code comm_close} message as soon as possible but there is never any
     * confirmation that the comm is open.
     *
     * @param targetName the name of the target on the frontend to message.
     * @param data       the payload to send with the close message.
     * @param factory    a comm producer. This is used to create the comm.
     * @param <T>        the type of {@link Comm} that the {@code factory} produces.
     *
     * @return a comm who's {@link Comm#send(JsonObject) send} method is targeted at a new comm
     *         create on the frontend by the target registered with the {@code targetName} or
     *         {@code null} if the manager could not open the comm.
     *         <p>
     *         The latter may happen if the manager is not connected to the frontend
     */
    public <T extends Comm> T openComm(String targetName, JsonObject data, CommFactory<T> factory);

    public default <T extends Comm> T openComm(String targetName, CommFactory<T> factory) {
        return this.openComm(targetName, null, factory);
    }

    /**
     * Send a message to a comm's frontend component. See {@link Comm#send(JsonObject, Map, List)} as well as
     * {@link Comm#send(JsonObject)} which is more likely the method to use as the metadata and blobs are lower level
     * constructs exposed for completeness but are often not necessary.
     * <p>
     * See {@link #messageComm(String, JsonObject)} for the higher level partner to this method.
     *
     * @param commID   the id of the target comm (or the id of the sending comm as both share the same id)
     * @param data     the data to send to the frontend
     * @param metadata any metadata to attach to the message being sent. May be {@code null} if no metadata is present.
     * @param blobs    any additional raw data to attach to the message. May be {@code null} if no blobs are present.
     */
    public void messageComm(String commID, JsonObject data, Map<String, Object> metadata, List<byte[]> blobs);

    /**
     * Send a message to a comm's frontend component. See {@link Comm#send(JsonObject)}
     *
     * @param commID the id of the target comm (or the id of the sending comm as both share the same id)
     * @param data   the data to send to the frontend
     */
    public void messageComm(String commID, JsonObject data);

    /**
     * Close both sides of a comm. This should be invoked whenever a comm is no longer
     * in use or destroyed, as a counterpart is living in the frontend. Failing to invoke this may
     * leak comm instances on the frontend as well as possibly leaving the manager holding on to
     * dead references. See {@link Comm#close()}.
     *
     * @param comm the comm to close.
     * @param data the payload to send with the close message.
     */
    public void closeComm(Comm comm, JsonObject data);

    public default void closeComm(Comm comm) {
        this.closeComm(comm, null);
    }

    public default void closeAll() {
        this.forEach(Comm::close);
    }

    /**
     * Register a target for comm creation at the frontend's request. A target must
     * first be registered in the kernel so that the frontend may ask to create a new
     * comm for speaking with the target.
     *
     * @param targetName the name of the target which must be specified by frontend's
     *                   opening up the communication
     * @param target     a {@link CommTarget} responsible for creating new comms at this
     *                   target name
     */
    public void registerTarget(String targetName, CommTarget target);

    /**
     * Unregister a target. This doesn't unregister comms with that target name but rather
     * prevents the target from creating anything new.
     * <p>
     * See also {@link #registerTarget(String, CommTarget)}
     *
     * @param targetName the name of the target to unregister
     */
    public void unregisterTarget(String targetName);

    /**
     * Lookup a target with the given name. See {@link #registerTarget(String, CommTarget)}
     *
     * @param targetName the target name to lookup
     *
     * @return the {@link CommTarget} registered with the {@code targetName}
     */
    public CommTarget getTarget(String targetName);
}

package io.github.spencerpark.jupyter.comm;

import com.google.gson.JsonObject;
import io.github.spencerpark.jupyter.api.comm.Comm;
import io.github.spencerpark.jupyter.api.comm.CommFactory;
import io.github.spencerpark.jupyter.api.comm.CommManager;
import io.github.spencerpark.jupyter.api.comm.CommTarget;
import io.github.spencerpark.jupyter.messages.MessageContext;

import java.util.*;

/**
 * A {@link CommManager} implementation that can be shared between the kernel and client implementations of the jupyter
 * comm protocol.
 */
public class DefaultCommManager implements CommManager {
    public static DefaultCommManager createConnectedTo(CommClient client) {
        DefaultCommManager manager = new DefaultCommManager();
        manager.connectTo(client);
        return manager;
    }

    protected final Map<String, CommTarget> targets = new HashMap<>();
    protected final Map<String, Comm> comms = new HashMap<>();
    protected final Deque<MessageContext> context = new LinkedList<>();

    protected CommClient client = null;

    /**
     * Connect to a (open or closed) client. After connecting this manager will send client
     * bound messages over this client. After connecting to a client, previous connections
     * are no longer used and comms connected to that client are disconnected.
     *
     * @param client the client to connect to.
     */
    public void connectTo(CommClient client) {
        if (this.client != null && this.client != client) {
            this.closeAll();
        }

        this.client = client;
    }

    /**
     * Push the most recent message context to attach to outgoing messages. For example, to link messages to evaluation
     * contexts.
     *
     * @param context the most recent message context.
     */
    public void pushContext(MessageContext context) {
        this.context.push(context);
    }

    /**
     * Drop a message context from the context stack. This will remove at most one (most recently pushed) context if
     * present in the stack.
     *
     * @param context the message context to drop.
     */
    public void dropContext(MessageContext context) {
        // A slightly safer pop.
        this.context.removeFirstOccurrence(context);
    }

    @Override
    public synchronized Iterator<Comm> iterator() {
        // TODO handlers should run on the same thread but double check that this doesn't need better synchronization
        return new ArrayList<>(this.comms.values()).iterator();
    }

    @Override
    public Comm getCommByID(String id) {
        return this.comms.get(id);
    }

    /**
     * Register a new comm that this manager should forward messages to in the event that
     * it receives one addressed to a comm with the the {@code comm}'s id.
     *
     * @param comm the comm to register with this handler
     */
    public void registerComm(Comm comm) {
        if (comm.getManager() != this) {
            throw new IllegalArgumentException("Can only register comms connected to this manager.");
        }

        this.comms.put(comm.getID(), comm);
    }

    /**
     * Unregister a comm from this manager. This prevents the manager from forwarding messages
     * to a previously {@link #registerComm(Comm) registered} comm with the {@code id}.
     *
     * @param id the id of the destination to unregister
     *
     * @return the comm that was unregistered or null if nothing was unregistered.
     */
    public Comm unregisterComm(String id) {
        return this.comms.remove(id);
    }

    @Override
    public <T extends Comm> T openComm(String targetName, JsonObject data, CommFactory<T> factory) {
        if (this.client == null)
            return null;

        String id = UUID.randomUUID().toString();

        T comm = this.client.sendOpen(this.context.peek(), id, targetName, data, msg ->
                factory.produce(this, id, targetName, msg));

        this.registerComm(comm);

        return comm;
    }

    @Override
    public void messageComm(String commID, JsonObject data, Map<String, Object> metadata, List<byte[]> blobs) {
        if (this.client != null) {
            this.client.sendMessage(this.context.peek(), commID, data, metadata, blobs);
        }
    }

    @Override
    public void messageComm(String commID, JsonObject data) {
        this.messageComm(commID, data, null, null);
    }

    @Override
    public void closeComm(Comm comm, JsonObject data) {
        Comm unregistered = this.unregisterComm(comm.getID());

        // Not great if the client is null but don't let it prevent closing this side.
        if (this.client != null) {
            this.client.sendClose(this.context.peek(), comm.getID(), data, msg -> {
                if (unregistered != null)
                    unregistered.onClose(msg, true);
            });
        }
    }

    @Override
    public void registerTarget(String targetName, CommTarget target) {
        this.targets.put(targetName, target);
    }

    @Override
    public void unregisterTarget(String targetName) {
        this.targets.remove(targetName);
    }

    @Override
    public CommTarget getTarget(String targetName) {
        return this.targets.get(targetName);
    }
}

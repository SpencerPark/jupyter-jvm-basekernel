package io.github.spencerpark.jupyter.kernel.debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.spencerpark.jupyter.messages.adapters.JsonBox;
import io.github.spencerpark.jupyter.messages.debug.DapCommandType;
import io.github.spencerpark.jupyter.messages.debug.DapErrorMessage;
import io.github.spencerpark.jupyter.messages.debug.DapEvent;
import io.github.spencerpark.jupyter.messages.debug.DapEventType;
import io.github.spencerpark.jupyter.messages.debug.DapProtocolMessage;
import io.github.spencerpark.jupyter.messages.debug.DapRequest;
import io.github.spencerpark.jupyter.messages.debug.DapResponse;
import io.github.spencerpark.jupyter.messages.debug.adapters.DapCommandTypeAdapter;
import io.github.spencerpark.jupyter.messages.debug.adapters.DapEventTypeAdapter;
import io.github.spencerpark.jupyter.messages.debug.adapters.DapProtocolMessageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DapProxy implements Debugger {
    private static final Logger LOG = LoggerFactory.getLogger(DapProxy.class);

    private final Gson gson = JsonBox.registerTypeAdapters(new GsonBuilder())
            .registerTypeAdapter(DapCommandType.class, DapCommandTypeAdapter.INSTANCE)
            .registerTypeAdapter(DapEventType.class, DapEventTypeAdapter.INSTANCE)
            .registerTypeAdapter(DapProtocolMessage.class, DapProtocolMessageAdapter.INSTANCE)
            .create();
    private final Gson untypedGson = JsonBox.registerTypeAdapters(new GsonBuilder())
            .registerTypeAdapter(DapCommandType.class, DapCommandTypeAdapter.UNTYPED_INSTANCE)
            .registerTypeAdapter(DapEventType.class, DapEventTypeAdapter.UNTYPED_INSTANCE)
            .registerTypeAdapter(DapProtocolMessage.class, DapProtocolMessageAdapter.INSTANCE)
            .create();

    private final AtomicInteger seq = new AtomicInteger();

    private final Set<DapEventPublisher> eventSubscribers = new HashSet<>();

    private final Map<DapCommandType<?, ?>, DapCommandHandler<?, ?>> requestHandlers = new HashMap<>();

    public final <A, B> void registerHandler(DapCommandType<A, B> type, DapCommandHandler<A, B> handler) {
        if (this.requestHandlers.put(type, handler) != null) {
            LOG.warn("Overwriting existing handler for {}", type);
        }
    }

    @SuppressWarnings("unchecked")
    public final <B> void registerNoArgHandler(DapCommandType<?, B> type, DapNoArgCommandHandler<B> handler) {
        this.registerHandler((DapCommandType<Object, B>) type, _args -> handler.handle());
    }

    @Override
    public Runnable subscribe(DapEventPublisher pub) {
        synchronized (this.eventSubscribers) {
            this.eventSubscribers.add(pub);
            return () -> {
                synchronized (this.eventSubscribers) {
                    this.eventSubscribers.remove(pub);
                }
            };
        }
    }

    private void publishDapEvent(DapEvent<?> event) {
        synchronized (this.eventSubscribers) {
            if (this.eventSubscribers.isEmpty()) {
                LOG.debug("No event subscribers, dropping '{}' event.", event.getEvent());
                return;
            }

            JsonElement jsonEvent = this.gson.toJsonTree(event);
            LOG.debug("Publishing '{}' event: {}", event.getEvent(), jsonEvent);

            for (DapEventPublisher pub : this.eventSubscribers) {
                try {
                    pub.emit(jsonEvent);
                } catch (Exception e) {
                    LOG.warn("Error publishing event:", e);
                }
            }
        }
    }

    protected final <B> void publishDapEvent(DapEventType<B> type, B body) {
        this.publishDapEvent(new DapEvent<>(this.seq.getAndIncrement(), type, body));
    }

    protected final <B> void publishDapEvent(DapEventType<B> type) {
        this.publishDapEvent(type, null);
    }

    protected final void publishDapEvent(JsonElement dapEvent) {
        DapEvent<?> event;
        try {
            event = this.untypedGson.fromJson(dapEvent, DapEvent.class);
        } catch (Exception e) {
            LOG.error("Cannot parse DAP event " + dapEvent + ". Skipping publish.", e);
            return;
        }

        this.publishDapEvent(event.withSeq(this.seq.getAndIncrement()));
    }

    /**
     * Forwards any commands not handled by this proxy. Typically, the jupyter specific extension commands would be
     * handled and the rest forwarded to an existing DAP server.
     *
     * @param dapRequest the wrapped request.
     * @return the wrapped reply.
     */
    protected abstract JsonElement forwardRequest(JsonElement dapRequest) throws DapException;

    protected DapErrorMessage wrapUnknownException(Exception e) {
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public JsonElement handleDapRequest(JsonElement jsonRequest) {
        DapRequest<?, ?> request = this.gson.fromJson(jsonRequest, DapRequest.class);
        DapCommandType commandType = request.getCommand();

        try {
            DapCommandHandler handler = this.requestHandlers.get(commandType);
            if (handler == null) {
                LOG.debug("No handler for {}. Forwarding request: json={}", commandType, jsonRequest);
                JsonElement forwardedJsonResponse = this.forwardRequest(jsonRequest);
                LOG.debug("Forwarded {} command returned: json={}", commandType, forwardedJsonResponse);
                DapResponse<?, ?> response = this.untypedGson.fromJson(forwardedJsonResponse, DapResponse.class)
                        .withSeq(this.seq.getAndIncrement());
                return this.gson.toJsonTree(response);
            } else {
                LOG.debug("Handling {}({} args): args={}", commandType, commandType.getArgumentsType(), request.getArguments());
                Object body = handler.handle(request.getArguments());
                LOG.debug("Handler for {} command returned: body={}", commandType, body);
                return this.gson.toJsonTree(
                        DapResponse.success(this.seq.getAndIncrement(), request.getSeq(), commandType, body));
            }
        } catch (DapException e) {
            return this.gson.toJsonTree(
                    DapResponse.error(this.seq.getAndIncrement(), request.getSeq(), commandType, e.getShortMessageCode(), e.toDapMessage()));
        } catch (Exception e) {
            DapErrorMessage msg = wrapUnknownException(e);
            if (msg == null) {
                LOG.error("Unhandled exception thrown while handling DAP request for " + commandType.getName() + ":", e);
                throw e;
            } else {
                return this.gson.toJsonTree(
                        DapResponse.error(this.seq.getAndIncrement(), request.getSeq(), commandType, null, msg));
            }
        }
    }
}

package io.github.spencerpark.jupyter.messages.debug.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import io.github.spencerpark.jupyter.messages.debug.DapCommandType;
import io.github.spencerpark.jupyter.messages.debug.DapEvent;
import io.github.spencerpark.jupyter.messages.debug.DapEventType;
import io.github.spencerpark.jupyter.messages.debug.DapProtocolMessage;
import io.github.spencerpark.jupyter.messages.debug.DapRequest;
import io.github.spencerpark.jupyter.messages.debug.DapResponse;

import java.lang.reflect.Type;

public class DapProtocolMessageAdapter implements JsonDeserializer<DapProtocolMessage> {
    public static final DapProtocolMessageAdapter INSTANCE = new DapProtocolMessageAdapter();

    private DapProtocolMessageAdapter() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DapProtocolMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject msgJson = json.getAsJsonObject();
        int seq = msgJson.getAsJsonPrimitive("seq").getAsInt();
        DapProtocolMessage.Type msgType = context.deserialize(msgJson.get("type"), DapProtocolMessage.Type.class);
        switch (msgType) {
            case REQUEST: {
                DapCommandType<?, ?> commandType = context.deserialize(msgJson.get("command"), DapCommandType.class);
                JsonElement argumentsJson = msgJson.get("arguments");
                if (argumentsJson == null || argumentsJson.isJsonNull()) {
                    return new DapRequest<>(seq, commandType);
                } else {
                    Object arguments = context.deserialize(argumentsJson, commandType.getArgumentsType());
                    return new DapRequest(seq, commandType, arguments);
                }
            }
            case RESPONSE: {
                int requestSeq = msgJson.getAsJsonPrimitive("request_seq").getAsInt();
                boolean success = msgJson.getAsJsonPrimitive("success").getAsBoolean();
                DapCommandType<?, ?> commandType = context.deserialize(msgJson.get("command"), DapCommandType.class);

                JsonPrimitive shortMessageJson = msgJson.getAsJsonPrimitive("message");
                String shortMessage = shortMessageJson == null ? null : shortMessageJson.getAsString();

                JsonElement bodyJson = msgJson.get("body");
                if (bodyJson == null || bodyJson.isJsonNull()) {
                    return new DapResponse<>(seq, requestSeq, commandType, success, shortMessage, null);
                } else if (success) {
                    Object body = context.deserialize(bodyJson, commandType.getBodyType());
                    return new DapResponse(seq, requestSeq, commandType, true, null, body);
                } else {
                    DapResponse.ErrorBody body = context.deserialize(bodyJson, DapResponse.ErrorBody.class);
                    return new DapResponse(seq, requestSeq, commandType, false, shortMessage, body);
                }
            }
            case EVENT: {
                DapEventType<?> eventType = context.deserialize(msgJson.get("event"), DapEventType.class);
                JsonElement bodyJson = msgJson.get("body");
                if (bodyJson == null || bodyJson.isJsonNull()) {
                    return new DapEvent<>(seq, eventType);
                } else {
                    Object body = context.deserialize(bodyJson, eventType.getBodyType());
                    return new DapEvent(seq, eventType, body);
                }
            }
            default:
                throw new JsonParseException("Unexpected message type: " + msgType);
        }
    }
}

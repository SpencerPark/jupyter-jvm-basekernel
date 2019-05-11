package io.github.spencerpark.jupyter.api.comm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public interface CommMessage {
    public boolean hasMetadata();

    public Map<String, Object> getMetadata();

    public Map<String, Object> getNonNullMetadata();

    public boolean hasBlobs();

    public List<byte[]> getBlobs();

    public List<byte[]> getNonNullBlobs();

    public static interface Open extends CommMessage {
        public String getCommID();

        public String getTargetName();

        public JsonElement getData();
    }

    public static interface Data extends CommMessage {
        public String getCommID();

        public JsonElement getData();
    }

    public static interface Close extends CommMessage {
        public String getCommID();

        public JsonElement getData();
    }
}

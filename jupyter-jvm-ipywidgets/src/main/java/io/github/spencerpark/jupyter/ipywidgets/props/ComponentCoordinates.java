package io.github.spencerpark.jupyter.ipywidgets.props;

import com.google.gson.annotations.SerializedName;

public class ComponentCoordinates {
    public static class Builder {
        private String name;
        private String module;
        private String version;

        private Builder() { }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public ComponentCoordinates create() {
            return ComponentCoordinates.of(this.name, this.module, this.version);
        }
    }

    public static ComponentCoordinates.Builder builder() {
        return new Builder();
    }

    public static ComponentCoordinates of(String name, String module, String version) {
        return new ComponentCoordinates(name, module, version);
    }

    private final String name;

    private final String module;

    @SerializedName("module_version")
    private final String version;

    private ComponentCoordinates(String name, String module, String version) {
        this.name = name;
        this.module = module;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }
}

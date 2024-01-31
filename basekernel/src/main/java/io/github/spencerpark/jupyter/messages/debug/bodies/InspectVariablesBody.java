package io.github.spencerpark.jupyter.messages.debug.bodies;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InspectVariablesBody {
    public static final class DefinedVariable {
        @SerializedName("name")
        private final String name;

        @SerializedName("variablesReference")
        private final int variablesReference;

        @SerializedName("value")
        private final String value;

        @SerializedName("type")
        private final String type;

        public DefinedVariable(String name, int variablesReference, String value, String type) {
            this.name = name;
            this.variablesReference = variablesReference;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getVariablesReference() {
            return variablesReference;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }

    @SerializedName("variables")
    protected final List<DefinedVariable> variables;

    public InspectVariablesBody(List<DefinedVariable> variables) {
        this.variables = variables;
    }

    public List<DefinedVariable> getVariables() {
        return variables;
    }
}

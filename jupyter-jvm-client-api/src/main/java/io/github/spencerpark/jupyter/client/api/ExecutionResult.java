package io.github.spencerpark.jupyter.client.api;

import io.github.spencerpark.jupyter.api.ExpressionValue;
import io.github.spencerpark.jupyter.api.display.DisplayData;

import java.util.Map;

public class ExecutionResult {
    private final int executionCount;
    private final DisplayData value;
    private final Map<String, ExpressionValue> userExpressions;

    public ExecutionResult(int executionCount, DisplayData value, Map<String, ExpressionValue> userExpressions) {
        this.executionCount = executionCount;
        this.value = value;
        this.userExpressions = userExpressions;
    }

    public int getExecutionCount() {
        return this.executionCount;
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public DisplayData getValue() {
        return this.value;
    }

    public Map<String, ExpressionValue> getUserExpressions() {
        return this.userExpressions;
    }
}

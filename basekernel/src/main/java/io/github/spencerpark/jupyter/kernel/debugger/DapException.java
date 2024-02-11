package io.github.spencerpark.jupyter.kernel.debugger;

import io.github.spencerpark.jupyter.messages.debug.DapErrorMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DapException extends RuntimeException {
    // Convention for names is if it starts with an underscore then the value doesn't contain PII and can be used for telemetry.
    private static final Pattern formatVariableRefPattern = Pattern.compile("\\{(?<var>[^}]*)}");

    public static String formatMessage(String format, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return format;
        }

        StringBuilder formattedMessage = new StringBuilder();
        Matcher matcher = formatVariableRefPattern.matcher(format);
        while (matcher.find()) {
            String variable = matcher.group("var");
            String value = variables.getOrDefault(variable, "");

            matcher.appendReplacement(formattedMessage, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(formattedMessage);

        return formattedMessage.toString();
    }

    public abstract int getTypeId();

    public String getShortMessageCode() {
        return null;
    }

    public boolean shouldSendTelemetry() {
        return false;
    }

    public boolean shouldShowUser() {
        return false;
    }

    public String getErrorHelpUrl() {
        return null;
    }

    public String getErrorHelpUrlLabel() {
        return null;
    }

    public String getRawMessage() {
        return super.getMessage();
    }

    public Map<String, String> getVariables() {
        return null;
    }

    @Override
    public String getMessage() {
        return formatMessage(this.getRawMessage(), this.getVariables());
    }

    public final DapErrorMessage toDapMessage() {
        return new DapErrorMessage(
                getTypeId(),
                this.getRawMessage(),
                this.getVariables(),
                this.shouldSendTelemetry() ? true : null,
                this.shouldShowUser() ? true : null,
                this.getErrorHelpUrl(),
                this.getErrorHelpUrlLabel()
        );
    }
}

package io.github.spencerpark.jupyter.api.magic.registry;

public class MagicArgsParseException extends RuntimeException {
    public MagicArgsParseException() {
    }

    public MagicArgsParseException(String format, Object... args) {
        super(String.format(format, args));
    }

    public MagicArgsParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MagicArgsParseException(Throwable cause) {
        super(cause);
    }
}

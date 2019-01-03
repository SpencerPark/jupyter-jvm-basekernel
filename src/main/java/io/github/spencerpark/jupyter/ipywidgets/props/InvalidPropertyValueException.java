package io.github.spencerpark.jupyter.ipywidgets.props;

public class InvalidPropertyValueException extends RuntimeException {
    public InvalidPropertyValueException() {
    }

    public InvalidPropertyValueException(String message) {
        super(message);
    }

    public InvalidPropertyValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPropertyValueException(Throwable cause) {
        super(cause);
    }
}

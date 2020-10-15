package io.github.spencerpark.jupyter.client.api;

import io.github.spencerpark.jupyter.api.display.DisplayData;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class Result<T> {
    public static <T> Result<T> success(T value, DisplayData pubValue) {
        return new Success<>(value, pubValue);
    }

    public static <T> Result<T> error(JupyterError reply, JupyterError pubValue) {
        return new Failure<>(reply, pubValue);
    }

    public static final class Success<T> extends Result<T> {
        private Success(Object value, Object pubValue) {
            super(value, pubValue);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Success[");
            sb.append(super.get());
            if (super.hasPublishedResult())
                sb.append(", pub=").append(super.getPublishedValue());
            sb.append("]");
            return sb.toString();
        }
    }

    public static final class Failure<T> extends Result<T> {
        public Failure(Object value, Object pubValue) {
            super(value, pubValue);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    private final Object value;
    private final Object pubValue;

    private Result(Object value, Object pubValue) {
        this.value = value;
        this.pubValue = pubValue;
    }

    public abstract boolean isSuccess();

    public boolean hasPublishedResult() {
        return this.pubValue != null;
    }

    public T get() {
        return (T) this.value;
    }

    public DisplayData getPublishedValue() {
        return (DisplayData) this.pubValue;
    }

    public JupyterError getError() {
        return (JupyterError) this.value;
    }

    public JupyterError getPublishedError() {
        return (JupyterError) this.pubValue;
    }

    // Utilities

    public T getOrThrow() throws JupyterReplyException {
        return this.getOrThrow(JupyterReplyException::new);
    }

    /**
     * Get the result value if successful. If unsuccessful, try and throw a
     * {@link JupyterPublishedException} with the {@link #getPublishedError()} if one
     * exists. Otherwise throw a {@link JupyterReplyException} with the {@link #getError()}.
     *
     * @return the {@link #get() result's value}
     *
     * @throws JupyterPublishedException if not {@link #isSuccess() successful} and {@link #hasPublishedResult() has a
     *                                   published error}
     * @throws JupyterReplyException     if not {@link #isSuccess() successful} and {@link #hasPublishedResult() doesn't
     *                                   have a published error}
     */
    public T getOrThrowPublished() throws JupyterPublishedException, JupyterReplyException {
        if (this.isSuccess())
            return this.get();
        else if (this.hasPublishedResult())
            throw new JupyterPublishedException(this.getPublishedError());
        else
            throw new JupyterReplyException(this.getError());
    }

    public <E extends Throwable> T getOrThrow(Function<JupyterError, E> ctor) throws E {
        if (this.isSuccess())
            return this.get();
        throw ctor.apply(this.getError());
    }

    public <E extends Throwable> T getOrThrow(BiFunction</* Reply */ JupyterError, /* Published */ JupyterError, E> ctor) throws E {
        if (this.isSuccess())
            return this.get();
        throw ctor.apply(this.getError(), this.getPublishedError());
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (this.isSuccess())
            return Result.success(mapper.apply(this.get()), this.getPublishedValue());
        return (Result<R>) this;
    }

    public <R> Result<R> map(BiFunction<T, DisplayData, R> mapper) {
        if (this.isSuccess())
            return Result.success(mapper.apply(this.get(), this.getPublishedValue()), this.getPublishedValue());
        return (Result<R>) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result<?> result = (Result<?>) o;
        return this.isSuccess() == result.isSuccess() &&
                Objects.equals(value, result.value) &&
                Objects.equals(pubValue, result.pubValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isSuccess(), value, pubValue);
    }
}

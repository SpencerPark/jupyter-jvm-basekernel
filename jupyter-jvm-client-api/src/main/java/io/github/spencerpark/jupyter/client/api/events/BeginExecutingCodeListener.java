package io.github.spencerpark.jupyter.client.api.events;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

@FunctionalInterface
public interface BeginExecutingCodeListener {
    public static BeginExecutingCodeListener of(ObjIntConsumer<String> consumer) {
        return new BeginExecutingCodeListener() {
            @Override
            public void onBeginExecutingCode(String code, int executionCount) {
                consumer.accept(code, executionCount);
            }

            @Override
            public void accept(String code, Integer executionCount) {
                consumer.accept(code, executionCount);
            }
        };
    }

    public static BeginExecutingCodeListener of(BiConsumer<String, Integer> consumer) {
        return new BeginExecutingCodeListener() {
            @Override
            public void onBeginExecutingCode(String code, int executionCount) {
                consumer.accept(code, executionCount);
            }

            @Override
            public void accept(String code, Integer executionCount) {
                consumer.accept(code, executionCount);
            }
        };
    }

    public void onBeginExecutingCode(String code, int executionCount);

    public default void accept(String code, Integer executionCount) {
        this.onBeginExecutingCode(code, Objects.requireNonNull(executionCount, "executionCount"));
    }
}

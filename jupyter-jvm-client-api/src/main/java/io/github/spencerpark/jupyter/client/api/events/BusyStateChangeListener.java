package io.github.spencerpark.jupyter.client.api.events;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface BusyStateChangeListener {
    public static BusyStateChangeListener of(Consumer<Boolean> consumer) {
        return new BusyStateChangeListener() {
            @Override
            public void onBusyStateChange(boolean busy) {
                consumer.accept(busy);
            }

            @Override
            public void accept(Boolean busy) {
                consumer.accept(busy);
            }
        };
    }

    public void onBusyStateChange(boolean busy);

    public default void accept(Boolean busy) {
        this.onBusyStateChange(Objects.requireNonNull(busy, "busy"));
    }
}

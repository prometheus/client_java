package io.prometheus.metrics.core.datapoints;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Convenience API for timing the duration of method calls.
 * <p>
 * All times are recorded in seconds, as Prometheus mandates seconds as the base unit.
 */
public interface TimerApi {

    /**
     * Start a {@code Timer}. Example:
     * <pre>{@code
     * try (Timer timer = dataPoint.startTimer) {
     *     // duration of this code block will be observed.
     * }
     * }</pre>
     * The duration is recorded in seconds, as Prometheus mandates seconds as the base unit.
     */
    Timer startTimer();

    /**
     * Observe the duration of the {@code func} call.
     * The duration is recorded in seconds, as Prometheus mandates seconds as the base unit.
     */
    default void time(Runnable func) {
        try (Timer timer = startTimer()) {
            func.run();
        }
    }

    /**
     * Observe the duration of the {@code func} call.
     * The duration is recorded in seconds, as Prometheus mandates seconds as the base unit.
     * @return the return value of {@code func}.
     */
    default <T> T time(Supplier<T> func) {
        try (Timer timer = startTimer()) {
            return func.get();
        }
    }

    /**
     * Like {@link #time(Supplier)}, but {@code func} may throw a checked {@code Exception}.
     */
    default <T> T timeChecked(Callable<T> func) throws Exception {
        try (Timer timer = startTimer()) {
            return func.call();
        }
    }
}

package io.prometheus.metrics.core.datapoints;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Convenience API for timing durations.
 * <p>
 * Durations are recorded in seconds. The Prometheus instrumentation guidelines <a href="https://prometheus.io/docs/instrumenting/writing_exporters/#naming">say</a>:
 * <i>"Metrics must use base units (e.g. seconds, bytes) and leave converting them to something more readable to graphing tools".</i>
 */
public interface TimerApi {

    /**
     * Start a {@code Timer}. Example:
     * <pre>{@code
     * Histogram histogram = Histogram.builder()
     *         .name("http_request_duration_seconds")
     *         .help("HTTP request service time in seconds")
     *         .unit(SECONDS)
     *         .labelNames("method", "path")
     *         .register();
     *
     * try (Timer timer = histogram.labelValues("GET", "/").startTimer()) {
     *     // duration of this code block will be observed.
     * }
     * }</pre>
     * Durations are recorded in seconds. The Prometheus instrumentation guidelines <a href="https://prometheus.io/docs/instrumenting/writing_exporters/#naming">say</a>:
     * <i>"Metrics must use base units (e.g. seconds, bytes) and leave converting them to something more readable to graphing tools".</i>
     */
    Timer startTimer();

    /**
     * Observe the duration of the {@code func} call. Example:
     * <pre>{@code
     * Histogram histogram = Histogram.builder()
     *         .name("request_duration_seconds")
     *         .help("HTTP request service time in seconds")
     *         .unit(SECONDS)
     *         .labelNames("method", "path")
     *         .register();
     *
     * histogram2.labelValues("GET", "/").time(() -> {
     *     // duration of this code block will be observed.
     * });
     * }</pre>
     * <p>
     * Durations are recorded in seconds. The Prometheus instrumentation guidelines <a href="https://prometheus.io/docs/instrumenting/writing_exporters/#naming">say</a>:
     * <i>"Metrics must use base units (e.g. seconds, bytes) and leave converting them to something more readable to graphing tools".</i>
     */
    default void time(Runnable func) {
        try (Timer timer = startTimer()) {
            func.run();
        }
    }

    /**
     * Like {@link #time(Runnable)}, but returns the return value of {@code func}.
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

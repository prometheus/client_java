package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.core.util.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * The ExemplarSampler selects Spans as exemplars.
 * <p>
 * There are two types of Exemplars: Regular exemplars are sampled implicitly if a supported tracing
 * library is detected. Custom exemplars are provided explicitly in code, for example if a developer
 * wants to make sure an Exemplar is created for a specific code path.
 * <p>
 * Spans will be marked as being an Exemplar by calling {@link SpanContext#markCurrentSpanAsExemplar()}.
 * The tracer implementation should set a Span attribute to mark the current Span as an Exemplar.
 * This attribute can be used by a trace sampling algorithm to make sure traces with Exemplars are sampled.
 * <p>
 * The ExemplarSample is rate-limited, so only a small fraction of Spans will be marked as Exemplars in
 * an application with a large number of requests.
 * <p>
 * See {@link ExemplarSamplerConfig} for configuration options.
 */
public class ExemplarSampler {

    private final ExemplarSamplerConfig config;
    private final Exemplar[] exemplars;
    private final Exemplar[] customExemplars; // Separate from exemplars, because we don't want custom exemplars
    // to be overwritten by automatic exemplar sampling. exemplars.lengt == customExemplars.length
    private final AtomicBoolean acceptingNewExemplars = new AtomicBoolean(true);
    private final AtomicBoolean acceptingNewCustomExemplars = new AtomicBoolean(true);
    private final SpanContext spanContext; // may be null, in that case SpanContextSupplier.getSpanContext() is used.

    public ExemplarSampler(ExemplarSamplerConfig config) {
        this(config, null);
    }

    /**
     * Constructor with an additional {code spanContext} argument.
     * This is useful for testing, but may also be useful in some production scenarios.
     * If {@code spanContext != null} that spanContext is used and
     * {@link io.prometheus.metrics.tracer.initializer.SpanContextSupplier SpanContextSupplier} is not used.
     * If {@code spanContext == null}
     * {@link io.prometheus.metrics.tracer.initializer.SpanContextSupplier#getSpanContext() SpanContextSupplier.getSpanContext()}
     * is called to find a span context.
     */
    public ExemplarSampler(ExemplarSamplerConfig config, SpanContext spanContext) {
        this.config = config;
        this.exemplars = new Exemplar[config.getNumberOfExemplars()];
        this.customExemplars = new Exemplar[exemplars.length];
        this.spanContext = spanContext;
    }

    public Exemplars collect() {
        // this may run in parallel with observe()
        long now = System.currentTimeMillis();
        List<Exemplar> result = new ArrayList<>(exemplars.length);
        for (int i = 0; i < customExemplars.length; i++) {
            Exemplar exemplar = customExemplars[i];
            if (exemplar != null) {
                if (now - exemplar.getTimestampMillis() > config.getMaxRetentionPeriodMillis()) {
                    customExemplars[i] = null;
                } else {
                    result.add(exemplar);
                }
            }
        }
        for (int i = 0; i < exemplars.length && result.size() < exemplars.length; i++) {
            Exemplar exemplar = exemplars[i];
            if (exemplar != null) {
                if (now - exemplar.getTimestampMillis() > config.getMaxRetentionPeriodMillis()) {
                    exemplars[i] = null;
                } else {
                    result.add(exemplar);
                }
            }
        }
        return Exemplars.of(result);
    }

    public void reset() {
        for (int i = 0; i < exemplars.length; i++) {
            exemplars[i] = null;
            customExemplars[i] = null;
        }
    }

    public void observe(double value) {
        if (!acceptingNewExemplars.get()) {
            return; // This is the hot path in a high-throughput application and should be as efficient as possible.
        }
        rateLimitedObserve(acceptingNewExemplars, value, exemplars, () -> doObserve(value));
    }

    public void observeWithExemplar(double value, Labels labels) {
        if (!acceptingNewCustomExemplars.get()) {
            return; // This is the hot path in a high-throughput application and should be as efficient as possible.
        }
        rateLimitedObserve(acceptingNewCustomExemplars, value, customExemplars, () -> doObserveWithExemplar(value, labels));
    }

    private long doObserve(double value) {
        if (exemplars.length == 1) {
            return doObserveSingleExemplar(value);
        } else if (config.getHistogramClassicUpperBounds() != null) {
            return doObserveWithUpperBounds(value);
        } else {
            return doObserveWithoutUpperBounds(value);
        }
    }

    private long doObserveSingleExemplar(double value) {
        long now = System.currentTimeMillis();
        Exemplar current = exemplars[0];
        if (current == null || now - current.getTimestampMillis() > config.getMinRetentionPeriodMillis()) {
            return updateExemplar(0, value, now);
        }
        return 0;
    }

    private long doObserveWithUpperBounds(double value) {
        long now = System.currentTimeMillis();
        double[] upperBounds = config.getHistogramClassicUpperBounds();
        for (int i = 0; i < upperBounds.length; i++) {
            if (value <= upperBounds[i]) {
                Exemplar previous = exemplars[i];
                if (previous == null || now - previous.getTimestampMillis() > config.getMinRetentionPeriodMillis()) {
                    return updateExemplar(i, value, now);
                } else {
                    return 0;
                }
            }
        }
        return 0; // will never happen, as upperBounds contains +Inf
    }

    private long doObserveWithoutUpperBounds(double value) {
        final long now = System.currentTimeMillis();
        Exemplar smallest = null;
        int smallestIndex = -1;
        Exemplar largest = null;
        int largestIndex = -1;
        int nullIndex = -1;
        for (int i = exemplars.length - 1; i >= 0; i--) {
            Exemplar exemplar = exemplars[i];
            if (exemplar == null) {
                nullIndex = i;
            } else if (now - exemplar.getTimestampMillis() > config.getMaxRetentionPeriodMillis()) {
                exemplars[i] = null;
                nullIndex = i;
            } else {
                if (smallest == null || exemplar.getValue() < smallest.getValue()) {
                    smallest = exemplar;
                    smallestIndex = i;
                }
                if (largest == null || exemplar.getValue() > largest.getValue()) {
                    largest = exemplar;
                    largestIndex = i;
                }
            }
        }
        if (nullIndex >= 0) {
            return updateExemplar(nullIndex, value, now);
        }
        if (now - smallest.getTimestampMillis() > config.getMinRetentionPeriodMillis() && value < smallest.getValue()) {
            return updateExemplar(smallestIndex, value, now);
        }
        if (now - largest.getTimestampMillis() > config.getMinRetentionPeriodMillis() && value > largest.getValue()) {
            return updateExemplar(largestIndex, value, now);
        }
        long oldestTimestamp = 0;
        int oldestIndex = -1;
        for (int i = 0; i < exemplars.length; i++) {
            Exemplar exemplar = exemplars[i];
            if (exemplar != null && exemplar != smallest && exemplar != largest) {
                if (oldestTimestamp == 0 || exemplar.getTimestampMillis() < oldestTimestamp) {
                    oldestTimestamp = exemplar.getTimestampMillis();
                    oldestIndex = i;
                }
            }
        }
        if (oldestIndex != -1 && now - oldestTimestamp > config.getMinRetentionPeriodMillis()) {
            return updateExemplar(oldestIndex, value, now);
        }
        return 0;
    }

    // Returns the timestamp of the newly added Exemplar (which is System.currentTimeMillis())
    // or 0 if no Exemplar was added.
    private long doObserveWithExemplar(double amount, Labels labels) {
        if (customExemplars.length == 1) {
            return doObserveSingleExemplar(amount, labels);
        } else if (config.getHistogramClassicUpperBounds() != null) {
            return doObserveWithExemplarWithUpperBounds(amount, labels);
        } else {
            return doObserveWithExemplarWithoutUpperBounds(amount, labels);
        }
    }

    private long doObserveSingleExemplar(double amount, Labels labels) {
        long now = System.currentTimeMillis();
        Exemplar current = customExemplars[0];
        if (current == null || now - current.getTimestampMillis() > config.getMinRetentionPeriodMillis()) {
            return updateCustomExemplar(0, amount, labels, now);
        }
        return 0;
    }

    private long doObserveWithExemplarWithUpperBounds(double value, Labels labels) {
        long now = System.currentTimeMillis();
        double[] upperBounds = config.getHistogramClassicUpperBounds();
        for (int i = 0; i < upperBounds.length; i++) {
            if (value <= upperBounds[i]) {
                Exemplar previous = customExemplars[i];
                if (previous == null || now - previous.getTimestampMillis() > config.getMinRetentionPeriodMillis()) {
                    return updateCustomExemplar(i, value, labels, now);
                } else {
                    return 0;
                }
            }
        }
        return 0; // will never happen, as upperBounds contains +Inf
    }

    private long doObserveWithExemplarWithoutUpperBounds(double amount, Labels labels) {
        final long now = System.currentTimeMillis();
        int nullPos = -1;
        int oldestPos = -1;
        Exemplar oldest = null;
        for (int i = customExemplars.length - 1; i >= 0; i--) {
            Exemplar exemplar = customExemplars[i];
            if (exemplar == null) {
                nullPos = i;
            } else if (now - exemplar.getTimestampMillis() > config.getMaxRetentionPeriodMillis()) {
                customExemplars[i] = null;
                nullPos = i;
            } else {
                if (oldest == null || exemplar.getTimestampMillis() < oldest.getTimestampMillis()) {
                    oldest = exemplar;
                    oldestPos = i;
                }
            }
        }
        if (nullPos != -1) {
            return updateCustomExemplar(nullPos, amount, labels, now);
        } else if (now - oldest.getTimestampMillis() > config.getMinRetentionPeriodMillis()) {
            return updateCustomExemplar(oldestPos, amount, labels, now);
        } else {
            return 0;
        }
    }

    /**
     * Observing requires a system call to {@link System#currentTimeMillis()},
     * and it requires iterating over the existing exemplars to check if one of the existing
     * exemplars can be replaced.
     * <p>
     * To avoid performance issues, we rate limit observing exemplars to
     * {@link ExemplarSamplerConfig#getSampleIntervalMillis()} milliseconds.
     */
    private void rateLimitedObserve(AtomicBoolean accepting, double value, Exemplar[] exemplars, LongSupplier observeFunc) {
        if (Double.isNaN(value)) {
            return;
        }
        if (!accepting.compareAndSet(true, false)) {
            return;
        }
        // observeFunc returns the current timestamp or 0 if no Exemplar was added.
        long now = observeFunc.getAsLong();
        long sleepTime = now == 0 ? config.getSampleIntervalMillis() : durationUntilNextExemplarExpires(now);
        Scheduler.schedule(() -> accepting.compareAndSet(false, true), sleepTime, TimeUnit.MILLISECONDS);
    }

    private long durationUntilNextExemplarExpires(long now) {
        long oldestTimestamp = now;
        for (Exemplar exemplar : exemplars) {
            if (exemplar == null) {
                return config.getSampleIntervalMillis();
            } else if (exemplar.getTimestampMillis() < oldestTimestamp) {
                oldestTimestamp = exemplar.getTimestampMillis();
            }
        }
        long oldestAge = now - oldestTimestamp;
        if (oldestAge < config.getMinRetentionPeriodMillis()) {
            return config.getMinRetentionPeriodMillis() - oldestAge;
        }
        return config.getSampleIntervalMillis();
    }

    private long updateCustomExemplar(int index, double value, Labels labels, long now) {
        if (!labels.contains(Exemplar.TRACE_ID) && !labels.contains(Exemplar.SPAN_ID)) {
            labels = labels.merge(doSampleExemplar());
        }
        customExemplars[index] = Exemplar.builder()
                .value(value)
                .labels(labels)
                .timestampMillis(now)
                .build();
        return now;
    }

    private long updateExemplar(int index, double value, long now) {
        Labels traceLabels = doSampleExemplar();
        if (!traceLabels.isEmpty()) {
            exemplars[index] = Exemplar.builder()
                    .value(value)
                    .labels(traceLabels)
                    .timestampMillis(now)
                    .build();
            return now;
        } else {
            return 0;
        }
    }

    private Labels doSampleExemplar() {
        // Using the qualified name so that Micrometer can exclude the dependency on prometheus-metrics-tracer-initializer
        // as they provide their own implementation of SpanContextSupplier.
        // If we had an import statement for SpanContextSupplier the dependency would be needed in any case.
        SpanContext spanContext = this.spanContext != null ? this.spanContext : io.prometheus.metrics.tracer.initializer.SpanContextSupplier.getSpanContext();
        try {
            if (spanContext != null) {
                if (spanContext.isCurrentSpanSampled()) {
                    String spanId = spanContext.getCurrentSpanId();
                    String traceId = spanContext.getCurrentTraceId();
                    if (spanId != null && traceId != null) {
                        spanContext.markCurrentSpanAsExemplar();
                        return Labels.of(Exemplar.TRACE_ID, traceId, Exemplar.SPAN_ID, spanId);
                    }
                }
            }
        } catch (NoClassDefFoundError ignored) {
        }
        return Labels.EMPTY;
    }
}

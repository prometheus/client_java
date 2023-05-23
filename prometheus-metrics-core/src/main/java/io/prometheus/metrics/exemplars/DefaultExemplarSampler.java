package io.prometheus.metrics.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.Labels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

public class DefaultExemplarSampler implements ExemplarSampler {

    private final ExemplarConfig config;
    protected final Exemplar[] exemplars;
    protected final Exemplar[] customExemplars; // Separate from exemplars, because we don't want custom exemplars
    // to be overwritten by automatic exemplar sampling. exemplars.lengt == customExemplars.length
    protected final AtomicBoolean acceptingNewExemplars = new AtomicBoolean(true);
    protected final AtomicBoolean acceptingNewCustomExemplars = new AtomicBoolean(true);

    /**
     * We want to keep each Exemplar for <tt>minAgeMillis</tt> milliseconds before we replace it with the next one
     * (see "rate-limited sampling" above). There are two ways to implement this:
     * <ol>
     *     <li>Call {@link System#currentTimeMillis()} for each observation to check the age of the current Exemplar.</li>
     *     <li>Run a background thread to schedule Exemplar updates.</li>
     * </ol>
     * The performance impact of {@link System#currentTimeMillis()} may be significant, for example if this is run
     * each time a counter metric is incremented. Therefore, we run a background thread.
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    private DefaultExemplarSampler(ExemplarConfig config) {
        this.config = config;
        double[] upperBounds = config.getUpperBounds();
        int numberOfExemplars = upperBounds != null ? upperBounds.length : config.getNumberOfExemplars();
        this.exemplars = new Exemplar[numberOfExemplars];
        this.customExemplars = new Exemplar[numberOfExemplars];
    }


    @Override
    public void reset() {
        for (int i=0; i<exemplars.length; i++) {
            exemplars[i] = null;
            customExemplars[i] = null;
        }
    }

    /**
     * Collect up to nExemplars exemplars, preferably custom exemplars.
     */
    @Override
    public Exemplars collect() {
        // this might run in parallel with observe()
        long now = System.currentTimeMillis();
        List<Exemplar> result = new ArrayList<>(exemplars.length);
        for (int i = 0; i < customExemplars.length; i++) {
            Exemplar exemplar = customExemplars[i];
            if (exemplar != null) {
                if (now - exemplar.getTimestampMillis() > config.getMaxAgeMillis()) {
                    customExemplars[i] = null;
                } else {
                    result.add(exemplar);
                }
            }
        }
        for (int i = 0; i < exemplars.length && result.size() < exemplars.length; i++) {
            Exemplar exemplar = exemplars[i];
            if (exemplar != null) {
                if (now - exemplar.getTimestampMillis() > config.getMaxAgeMillis()) {
                    exemplars[i] = null;
                } else {
                    result.add(exemplar);
                }
            }
        }
        return Exemplars.of(result); // TODO
    }

    @Override
    public void observe(double value) {
        if (!acceptingNewExemplars.get()) {
            // This is the hot path. This should be as efficient as possible.
            return;
        }
        rateLimitedObserve(acceptingNewExemplars, value, exemplars, () -> doObserve(value));
    }

    @Override
    public void observeWithExemplar(double value, Labels labels) {
        if (!acceptingNewCustomExemplars.get()) {
            // This is the hot path. This should be as efficient as possible.
            return;
        }
        rateLimitedObserve(acceptingNewCustomExemplars, value, customExemplars, () -> doObserveWithExemplar(value, labels));
    }


    private long doObserve(double value) {
        if (exemplars.length == 1) {
            return doObserveSingleExemplar(value);
        } else if (config.getUpperBounds() != null) {
            return doObserveWithUpperBounds(value, config.getUpperBounds());
        } else {
            return doObserveWithoutUpperBounds(value);
        }
    }

    private long doObserveSingleExemplar(double value) {
        long now = System.currentTimeMillis();
        Exemplar current = exemplars[0];
        if (current == null || now - current.getTimestampMillis() > config.getMinAgeMillis()) {
            return updateExemplar(0, value, now);
        }
        return 0;
    }

    private long doObserveWithUpperBounds(double value, double[] upperBounds) {
        long now = System.currentTimeMillis();
        for (int i=0; i<upperBounds.length; i++) {
            if (upperBounds[i] >= value) {
                Exemplar previous = exemplars[i];
                if (previous == null || now - previous.getTimestampMillis() > config.getMinAgeMillis()) {
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
            } else if (now - exemplar.getTimestampMillis() > config.getMaxAgeMillis()) {
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
        if (now - smallest.getTimestampMillis() > config.getMinAgeMillis() && value < smallest.getValue()) {
            return updateExemplar(smallestIndex, value, now);
        }
        if (now - largest.getTimestampMillis() > config.getMinAgeMillis() && value > largest.getValue()) {
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
        if (oldestIndex != -1 && now - oldestTimestamp > config.getMinAgeMillis()) {
            return updateExemplar(oldestIndex, value, now);
        }
        return 0;
    }

    // Returns the timestamp of the newly added Exemplar (which is System.currentTimeMillis())
    // or 0 if no Exemplar was added.
    private long doObserveWithExemplar(double amount, Labels labels) {
        if (customExemplars.length == 1) {
            return doObserveSingleExemplar(amount, labels);
        } else if (config.getUpperBounds() != null) {
            return doObserveWithExemplarWithUpperBounds(amount, labels, config.getUpperBounds());
        } else {
            return doObserveWithExemplarWithoutUpperBounds(amount, labels);
        }
    }

    private long doObserveSingleExemplar(double amount, Labels labels) {
        long now = System.currentTimeMillis();
        Exemplar current = customExemplars[0];
        if (current == null || now - current.getTimestampMillis() > config.getMinAgeMillis()) {
            return updateCustomExemplar(0, amount, labels, now);
        }
        return 0;
    }

    private long doObserveWithExemplarWithUpperBounds(double value, Labels labels, double[] upperBounds) {
        long now = System.currentTimeMillis();
        for (int i=0; i<upperBounds.length; i++) {
            if (upperBounds[i] > value) {
                Exemplar previous = customExemplars[i];
                if (previous == null || now - previous.getTimestampMillis() > config.getMinAgeMillis()) {
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
            } else if (now - exemplar.getTimestampMillis() > config.getMaxAgeMillis()) {
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
        } else if (now - oldest.getTimestampMillis() > config.getMinAgeMillis()) {
            return updateCustomExemplar(oldestPos, amount, labels, now);
        } else {
            return 0;
        }
    }

    private void rateLimitedObserve(AtomicBoolean accepting, double value, Exemplar[] exemplars, LongSupplier observeFunc) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }
        if (!accepting.compareAndSet(true, false)) {
            return;
        }
        // observeFunc returns the timestamp of the newly added Exemplar (which is System.currentTimeMillis())
        // or 0 if no Exemplar was added.
        long now = observeFunc.getAsLong();
        long delayMillis = config.getSampleIntervalMillis();
        if (now != 0) {
            // An Exemplar was updated. Find the age of the oldest Exemplar and set the delay accordingly.
            long oldestTimestamp = now;
            for (Exemplar exemplar : exemplars) {
                if (exemplar == null) {
                    oldestTimestamp = 0;
                } else if (exemplar.getTimestampMillis() < oldestTimestamp) {
                    oldestTimestamp = exemplar.getTimestampMillis();
                }
            }
            long oldestAge = now - oldestTimestamp;
            if (oldestAge < config.getMinAgeMillis()) {
                delayMillis = config.getMinAgeMillis() - oldestAge;
            }
        }
        executor.schedule(() -> accepting.compareAndSet(false, true), delayMillis, TimeUnit.MILLISECONDS);
    }

    protected long updateCustomExemplar(int index, double value, Labels labels, long now) {
        if (!labels.contains(Exemplar.TRACE_ID) && !labels.contains(Exemplar.SPAN_ID)) {
            labels = labels.merge(newTraceLabels());
        }
        customExemplars[index] = Exemplar.newBuilder()
                .withValue(value)
                .withLabels(labels)
                .withTimestampMillis(now)
                .build();
        return now;
    }

    protected long updateExemplar(int index, double value, long now) {
        Labels traceLabels = newTraceLabels();
        if (!traceLabels.isEmpty()) {
            exemplars[index] = Exemplar.newBuilder()
                    .withValue(value)
                    .withLabels(traceLabels)
                    .withTimestampMillis(now)
                    .build();
            return now;
        } else {
            return 0;
        }
    }

    private Labels newTraceLabels() {
        Object spanContextSupplierObject = config.getSpanContextSupplier();
        if (spanContextSupplierObject != null) {
            try {
                SpanContextSupplier spanContextSupplier = (SpanContextSupplier) spanContextSupplierObject;
                if (spanContextSupplier.isSampled()) {
                    String spanId = spanContextSupplier.getSpanId();
                    String traceId = spanContextSupplier.getTraceId();
                    if (spanId != null && traceId != null) {
                        return Labels.of(Exemplar.TRACE_ID, traceId, Exemplar.SPAN_ID, spanId);
                    }
                }
            } catch (NoClassDefFoundError ignored) {
            }
        }
        return Labels.EMPTY;
    }

    /**
     * For unit test.
     */
    void awaitInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        executor.schedule(latch::countDown, 0, TimeUnit.MILLISECONDS);
        latch.await();
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }

    public static ExemplarSampler newInstance(ExemplarConfig config) {
        return new DefaultExemplarSampler(config);
    }
}

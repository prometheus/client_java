package io.prometheus.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static io.prometheus.client.CollectorClock.DEFAULT_CLOCK;

/**
 * Extremum metric, to report maximum/minimum values observed within within configurable sliding window.
 * <p>
 * Examples of ExtremumCollector include:
 * <ul>
 *  <li>The worst http request handling time</li>
 *  <li>The smallest message latency on network channel</li>
 *  <li>Biggest GC pauses</li>
 *  <li>Biggest message processing queue size</li>
 * </ul>
 *
 * Extremum metric can go up and down in time.
 * You can look at it as a gauge that shows you only biggest/smallest values.
 *
 * In order for this collector to work properly it should be configured correctly.
 * I.e. sampling period should be somewhere between 5 and 90 seconds.
 * Sample rate should be at least 2 and not too big (< 100) in order to keep collector's performance on high level.
 *
 * By default ExtremumCollector has sample period of 60 seconds and sample rate of 60, which gives 1 second discretion.
 *
 * <p>
 * An example ExtremumCollector:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final ExtremumCollector maxInProgressRequests = ExtremumCollector.build()
 *         .name("max_in_progress_requests").help("Biggest amount of the request in process in time.").register();
 *     static final AtomicInteger inProgressRequests = new AtomicInteger(0);
 *
 *     void processRequest() {
 *        maxInProgressRequests.set(inProgressRequests.incrementAndGet());
 *        // Your code here.
 *        inProgressRequests.decrementAndGet();
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * You can also use labels to track different types of metric:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Gauge maxInProgressRequests = ExtremumCollector.build()
 *           .name("max_in_progress_requests").help("Biggest amount of the request in process in time.")
 *           .labelNames("method").register();
 *     static final AtomicInteger inProgressGetRequests = new AtomicInteger(0);
 *     static final AtomicInteger inProgressPostRequests = new AtomicInteger(0);
 *
 *     void processGetRequest() {
 *        inprogressRequests.labels("get").set(inProgressGetRequests.incrementAndGet());
 *        // Your code here.
 *        inProgressGetRequests.decrementAndGet();
 *     }
 *     void processPostRequest() {
 *        inprogressRequests.labels("post").set(inProgressPostRequests.incrementAndGet());
 *        // Your code here.
 *        inProgressPostRequests.decrementAndGet();
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * These can be aggregated and processed together much more easily in the Prometheus
 * server than individual metrics for each labelset.
 */
public class ExtremumCollector extends SimpleCollector<ExtremumCollector.Child> implements Collector.Describable {

    private final Direction direction;
    private final long samplingPeriodNanos;
    private final int samplingRate;
    private final CollectorClock clock;

    private ExtremumCollector(Builder b) {
        super(b);
        direction = b.direction;
        samplingPeriodNanos = b.samplingPeriodNanos;
        samplingRate = b.samplingRate;
        clock = b.clock;
        initializeNoLabelsChild();
    }

    public static class Builder extends SimpleCollector.Builder<Builder, ExtremumCollector> {

        private Direction direction = Direction.HIGH;
        private long samplingPeriodNanos = TimeUnit.SECONDS.toNanos(60);
        private int samplingRate = 60;
        private CollectorClock clock = DEFAULT_CLOCK;

        Builder setDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        Builder setSamplingPeriod(long period, TimeUnit unit) {
            this.samplingPeriodNanos = unit.toNanos(period);
            return this;
        }

        Builder setSamplingRate(int rate) {
            if (rate < 2) throw new IllegalArgumentException("Rate can not be smaller than 2.");
            this.samplingRate = rate;
            return this;
        }

        Builder withClock(CollectorClock clock) {
            this.clock = clock;
            return this;
        }


        @Override
        public ExtremumCollector create() {
            dontInitializeNoLabelsChild = true;
            return new ExtremumCollector(this);
        }
    }

    /**
     * Return a Builder to allow configuration of a new ExtremumCollector. Ensures required fields are provided.
     *
     * @param name The name of the metric
     * @param help The help string of the metric
     */
    public static Builder build(String name, String help) {
        return new Builder().name(name).help(help);
    }

    /**
     * Return a Builder to allow configuration of a new ExtremumCollector.
     */
    public static Builder build() {
        return new Builder();
    }

    @Override
    protected Child newChild() {
        return new Child(clock, direction, (samplingPeriodNanos) / samplingRate, samplingRate);
    }

    /**
     * The value of a single ExtremumCollector.
     * <p>
     * <em>Warning:</em> References to a Child become invalid after using
     * {@link SimpleCollector#remove} or {@link SimpleCollector#clear},
     */
    public static class Child {
        private final Direction direction;
        private final CollectorClock clock;
        private final long samplingPeriodNanos;
        private final int numberOfSamples;

        private final AtomicReferenceArray<Tuple> extrema;

        public Child(CollectorClock clock, Direction direction, long samplingPeriodNanos, int numberOfSamples) {
            this.clock = clock;
            this.direction = direction;
            this.samplingPeriodNanos = samplingPeriodNanos;
            this.numberOfSamples = numberOfSamples;

            this. extrema = new AtomicReferenceArray<Tuple>(numberOfSamples);
        }

        /**
         * Tries to set a new extremum in sample bucket or ignores provided value if it is not an extrememum.
         *
         * @throws IllegalArgumentException If amt is negative.
         */
        public void set(double extr) {
            long sampleTime = clock.nanos();
            int point = getSampleIndex(sampleTime, samplingPeriodNanos, numberOfSamples);
            Tuple current = extrema.get(point);
            if (current == null || direction.compare(current.getExtremum(),extr)) {
                Tuple candidate = new Tuple(extr, sampleTime);
                do {
                    current = extrema.get(point);
                } while ((current == null || direction.compare(current.getExtremum(), extr))
                        && !extrema.compareAndSet(point, current, candidate));
            }
        }

        private int getSampleIndex(long sampleTime, long samplingPeriod, long numberOfSamples) {
            return (int)((sampleTime % (samplingPeriod * numberOfSamples)) / samplingPeriod);
        }

        /**
         * Gets extremum across all the samples buckets that are not outdated
         */
        public double get() {
            long sampleEnd = clock.nanos();
            double result = direction.getStartingValue();
            for(int i = 0; i < extrema.length(); i++) {
                Tuple candidate = extrema.get(i);

                if(candidate != null &&
                        (sampleEnd - candidate.getTimestamp()) <= (samplingPeriodNanos * numberOfSamples)
                        && direction.compare(result, candidate.getExtremum())) {
                    result = candidate.getExtremum();
                }
            }
            return result == direction.getStartingValue()? 0 : result;
        }
    }

    public enum Direction {
        HIGH {
            @Override
            boolean compare(double current, double candidate) {
                return candidate > current;
            }

            @Override
            double getStartingValue() {
                return -Double.MAX_VALUE;
            }
        },

        LOW {
            @Override
            boolean compare(double current, double candidate) {
                return candidate < current;
            }

            @Override
            double getStartingValue() {
                return Double.MAX_VALUE;
            }
        };

        abstract boolean compare(double current, double candidate);
        abstract double getStartingValue();
    }

    private static class Tuple {
        private final double extremum;
        private final long timestamp;

        Tuple(double extremum, long timestamp) {
            this.extremum = extremum;
            this.timestamp = timestamp;
        }

        double getExtremum() {
            return extremum;
        }

        long getTimestamp() {
            return timestamp;
        }
    }

    // Convenience methods.
    /**
     * Sets the extremum with no labels
     *
     * @throws IllegalArgumentException If amt is negative.
     */
    public void set(double amt) {
        noLabelsChild.set(amt);
    }

    /**
     * Gets extremum across all the samples buckets that are not outdated
     */
    public double get() {
        return noLabelsChild.get();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>(children.size());
        for(Map.Entry<List<String>, ExtremumCollector.Child> c: children.entrySet()) {
            samples.add(new MetricFamilySamples.Sample(fullname, labelNames, c.getKey(), c.getValue().get()));
        }
        return familySamplesList(Type.GAUGE, samples);
    }

    @Override
    public List<MetricFamilySamples> describe() {
        return Collections.<MetricFamilySamples>singletonList(new GaugeMetricFamily(fullname, help, labelNames));
    }
}
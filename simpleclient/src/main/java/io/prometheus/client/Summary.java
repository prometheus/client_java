package io.prometheus.client;

import io.prometheus.client.stream.CKMSStream;
import io.prometheus.client.stream.Quantile;
import io.prometheus.client.stream.Stream;

import java.util.*;

/**
 * Summary metric, to track the size of events.
 * <p>
 * Example of uses for Summaries include:
 * <ul>
 * <li>Response latency</li>
 * <li>Request size</li>
 * </ul>
 * <p>
 * <p>
 * Example Summaries:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Summary receivedBytes = Summary.build()
 *         .name("requests_size_bytes").help("Request size in bytes.").register();
 *     static final Summary requestLatency = Summary.build()
 *         .name("requests_latency_seconds").help("Request latency in seconds.").register();
 *
 *     void processRequest(Request req) {
 *        Summary.Timer requestTimer = requestLatency.startTimer();
 *        try {
 *          // Your code here.
 *        } finally {
 *          receivedBytes.observe(req.size());
 *          requestTimer.observeDuration();
 *        }
 *     }
 *   }
 * }
 * </pre>
 * This would allow you to track request rate, average latency and average request size.
 */
public class Summary extends SimpleCollector<Summary.Child> {
    // Quantiles that should be calculated for this Summary
    // e.g.  0.20, 0.50, 0.99
    private Quantile[] quantiles;
    private Stream stream;
    private boolean shouldCalculateQuant = false;

    Summary(Builder b) {
        super(b);
        quantiles = b.quantiles;
        this.stream = b.stream;
        this.shouldCalculateQuant = b.shouldCalculateQuant;
    }

    public static class Builder extends SimpleCollector.Builder<Builder, Summary> {
        private Quantile[] quantiles = new Quantile[]{new Quantile(0.2, 0.05), new Quantile(0.5, 0.1),
                new Quantile(0.9, 0.01)};

        private Stream<Double> stream;
        private boolean shouldCalculateQuant;
        @Override
        public Summary create() {
            return new Summary(this);
        }

        public Builder quantiles(Quantile... quantiles) {
            // We want the Summary to display quantiles in ascending order (e.g. 0.20, 0.50, 0.99)
            //Arrays.sort(quantiles);
            this.quantiles = quantiles;
            this.shouldCalculateQuant = true;
            return this;
        }

        public Builder stream(Stream stream) {
            this.stream = stream;
            this.shouldCalculateQuant = true;
            return this;
        }
    }

    protected boolean shouldCalculateQuantiles() {
        return shouldCalculateQuant;
    }

    /**
     * Return a Builder to allow configuration of a new Summary.
     */
    public static Builder build() {
        return new Builder();
    }


    @Override
    protected Child newChild() {
        if (this.shouldCalculateQuantiles()) {
            if(this.stream == null){
                this.stream = new CKMSStream(500, quantiles);
            }
            return new Child(this.stream);
        } else {
            return new Child();
        }
    }

    /**
     * Represents an event being timed.
     */
    public static class Timer {
        Child child;
        long start;

        private Timer(Child child) {
            this.child = child;
            start = Child.timeProvider.nanoTime();
        }

        /**
         * Observe the amount of time in seconds since {@link Child#startTimer} was called.
         *
         * @return Measured duration in seconds since {@link Child#startTimer} was called.
         */
        public double observeDuration() {
            double elapsed = (Child.timeProvider.nanoTime() - start) / NANOSECONDS_PER_SECOND;
            child.observe(elapsed);
            return elapsed;
        }
    }

    /**
     * The value of a single Summary.
     * <p>
     * <em>Warning:</em> References to a Child become invalid after using
     * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
     */
    public static class Child {
        public static class Value {
            private double count;
            private double sum;
        }

        Child() {

        }

        Child(Stream<Double> stream) {
            this.stream = stream;
            shouldCalculateQuant = true;
        }

        // Having these seperate leaves us open to races,
        // however Prometheus as whole has other races
        // that mean adding atomicity here wouldn't be useful.
        // This should be reevaluated in the future.
        private DoubleAdder count = new DoubleAdder();
        private DoubleAdder sum = new DoubleAdder();

        // Boolean flag that dictates whether a Child instance should save data for subsequent quantile
        // calculations.
        private boolean shouldCalculateQuant = false;
        private Stream<Double> stream;

        static TimeProvider timeProvider = new TimeProvider();

        /**
         * Observe the given amount.
         */
        public void observe(double amt) {
            count.add(1);
            sum.add(amt);
            if (this.shouldCalculateQuant) {

                //  TODO
                //  Will need to look into replacing the requirement.
                synchronized (stream) {
                    stream.insert(amt);
                }
            }
        }

        /**
         * Start a timer to track a duration.
         * <p>
         * Call {@link Timer#observeDuration} at the end of what you want to measure the duration of.
         */
        public Timer startTimer() {
            return new Timer(this);
        }

        /**
         * Get the value of the Summary.
         * <p>
         * <em>Warning:</em> The definition of {@link Value} is subject to change.
         */
        public Value get() {
            Value v = new Value();
            v.count = count.sum();
            v.sum = sum.sum();
            return v;
        }

    }

    // Convenience methods.


    /**
     * Observe the given amount on the summary with no labels.
     */
    public void observe(double amt) {
        noLabelsChild.observe(amt);
    }

    /**
     * Start a timer to track a duration on the summary with no labels.
     * <p>
     * Call {@link Timer#observeDuration} at the end of what you want to measure the duration of.
     */
    public Timer startTimer() {
        return noLabelsChild.startTimer();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
        for (Map.Entry<List<String>, Child> c : children.entrySet()) {
            Child.Value v = c.getValue().get();

            samples.add(new MetricFamilySamples.Sample(fullname + "_count", labelNames, c.getKey(), v.count));
            samples.add(new MetricFamilySamples.Sample(fullname + "_sum", labelNames, c.getKey(), v.sum));


            // If the quantiles is a desired calculation then add the quantile calculations to the Summary.
            if (this.shouldCalculateQuantiles()) {
                // Since `labelNames` is declared final, we need to create a new list that includes our "quantile"
                // label.

                Map<Quantile, Double> snapshot;

                //  TODO
                //  Will need to look into replacing the requirement.
                synchronized (stream) {
                   snapshot = c.getValue().stream.getSnapshot(this.quantiles);
                }
                List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
                labelNamesWithQuantile.add("quantile");

                for (Map.Entry<Quantile, Double> qe : snapshot.entrySet()) {
                    List<String> labelValuesWithQuantile = new ArrayList<String>(c.getKey());

                    labelValuesWithQuantile.add(Double.toString(qe.getKey().getQuantile()));
                    samples.add(new MetricFamilySamples.Sample(fullname, labelNamesWithQuantile, labelValuesWithQuantile,
                            qe.getValue()));
                }
            }
        }

        MetricFamilySamples mfs = new MetricFamilySamples(fullname, Type.SUMMARY, help, samples);
        List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>();
        mfsList.add(mfs);
        return mfsList;
    }


    static class TimeProvider {
        long nanoTime() {
            return System.nanoTime();
        }
    }
}

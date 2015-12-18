package io.prometheus.client;

import java.util.*;

import static java.lang.Math.floor;


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

    private double[] quantiles;

    Summary(Builder b) {
        super(b);
        this.quantiles = b.quantiles;
    }

    public static class Builder extends SimpleCollector.Builder<Builder, Summary> {
        private double[] quantiles;

        @Override
        public Summary create() {
            return new Summary(this);
        }

        public Builder quantiles(double... quantiles) {
            this.quantiles = quantiles;
            return this;
        }
    }

    /**
     * Return a Builder to allow configuration of a new Summary.
     */
    public static Builder build() {
        return new Builder();
    }


    @Override
    protected Child newChild() {
        return new Child();
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

        // Having these seperate leaves us open to races,
        // however Prometheus as whole has other races
        // that mean adding atomicity here wouldn't be useful.
        // This should be reevaluated in the future.
        private DoubleAdder count = new DoubleAdder();
        private DoubleAdder sum = new DoubleAdder();
        private List<Double> data = new ArrayList<Double>();
        static TimeProvider timeProvider = new TimeProvider();

        /**
         * Observe the given amount.
         */
        public void observe(double amt) {
            count.add(1);
            sum.add(amt);
            data.add(amt);
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


        /***
         * Source for this algorithm:
         * "http://www.dummies.com/how-to/content/how-to-calculate-percentiles-in-statistics.html"
         *
         * @param quantile
         * @return
         */
        public double getQuantile(double quantile) {
            if (quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)) {
                throw new IllegalArgumentException(quantile + " is not in [0..1]");
            }

            if (data.size() == 0) {
                return 0.0;
            }

            final double pos = quantile * (data.size());
            final Boolean wholeNumber = pos == Math.floor(pos) && !Double.isInfinite(pos);
            final int index = wholeNumber? (int) pos - 1: (int) Math.ceil(pos) - 1;

            if (index < 1) {
                return data.get(0);
            }

            if (index >= data.size()) {
                return data.get(data.size() - 1);
            }

            if (wholeNumber) {
                if (data.size() >= index + 1) {
                    return (data.get(index) + data.get(index + 1)) / 2;
                }
                return data.get(index);
            } else {
                return data.get(index);
            }
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
            if (quantiles != null && quantiles.length > 0) {
                // We want the Summary to display quantiles in ascending order (e.g. 0.20, 0.50, 0.99)
                Arrays.sort(quantiles);

                // Data needs to be sorted. This is a pre-requisite for percentile/quantile calculations.
                Collections.sort(c.getValue().data);

                // Since `labelNames` is declared final, we need to create a new list that includes our "quantile"
                // label.
                List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
                labelNamesWithQuantile.add("quantile");

                for (int i = 0; i < quantiles.length; i++) {
                    List<String> labelValuesWithQuantile = new ArrayList<String>(c.getKey());
                    labelValuesWithQuantile.add(Double.toString(quantiles[i]));
                    samples.add(new MetricFamilySamples.Sample(fullname, labelNamesWithQuantile, labelValuesWithQuantile,
                            c.getValue().getQuantile(quantiles[i])));
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

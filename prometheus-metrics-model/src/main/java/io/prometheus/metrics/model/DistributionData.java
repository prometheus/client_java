package io.prometheus.metrics.model;

/**
 * Common base class for static histogram data, native histogram data, and summary data.
 */
public abstract class DistributionData extends MetricData {
    private final long count;
    private final double sum;
    private final Exemplars exemplars;

    // TODO: In OpenMetrics, count and sum are either both present or both absent. Do we want to enforce this?
    protected DistributionData(long count, double sum, Exemplars exemplars, Labels labels, long createdTimestampMillis, long scrapeTimestampMillis) {
        super(labels, createdTimestampMillis, scrapeTimestampMillis);
        this.count = count;
        this.sum = sum;
        this.exemplars = exemplars;
        if (exemplars == null) {
            throw new NullPointerException("Exemplars cannot be null. Use Exemplars.EMPTY if there are no Exemplars.");
        }
    }

    public boolean hasCount() {
        return count >= 0;
    }

    public boolean hasSum() {
        return !Double.isNaN(sum);
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public Exemplars getExemplars() {
        return exemplars;
    }

    public static abstract class Builder<T extends Builder<T>> extends MetricData.Builder<T> {

        protected long count = -1;
        protected double sum = Double.NaN;
        protected long createdTimestampMillis = 0L;
        protected Exemplars exemplars = Exemplars.EMPTY;

        /**
         * Count can be explicitly set on summaries (this is a public method for summary metrics),
         * and it is set implicitly on histograms (must be the same value as the +Inf bucket).
         */
        protected T withCount(long count) {
            this.count = count;
            return self();
        }

        public T withSum(double sum) {
            this.sum = sum;
            return self();
        }

        public T withExemplars(Exemplars exemplars) {
            this.exemplars = exemplars;
            return self();
        }

        public T withCreatedTimestampMillis(long createdTimestampMillis) {
            this.createdTimestampMillis = createdTimestampMillis;
            return self();
        }
    }
}

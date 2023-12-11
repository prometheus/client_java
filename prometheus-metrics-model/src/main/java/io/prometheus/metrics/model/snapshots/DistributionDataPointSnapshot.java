package io.prometheus.metrics.model.snapshots;

/**
 * Common base class for histogram and summary data.
 * Histograms and Summaries represent distributions, like a latency distribution or a distribution
 * of request sizes in Bytes.
 */
public abstract class DistributionDataPointSnapshot extends DataPointSnapshot {
    private final long count; // optional, negative value means no count.
    private final double sum; // optional, Double.NaN means no sum.
    private final Exemplars exemplars; // optional, Exemplars.EMPTY means no Exemplars.

    /**
     * See JavaDoc of the child classes.
     */
    protected DistributionDataPointSnapshot(long count, double sum, Exemplars exemplars, Labels labels, long createdTimestampMillis, long scrapeTimestampMillis) {
        super(labels, createdTimestampMillis, scrapeTimestampMillis);
        this.count = count;
        this.sum = sum;
        this.exemplars = exemplars == null ? Exemplars.EMPTY : exemplars;
        validate();
    }

    private void validate() {
        // If a histogram or summary observes negative values the sum could be negative.
        // According to OpenMetrics sum should be omitted in that case, but we don't enforce this here.
    }

    public boolean hasCount() {
        return count >= 0;
    }

    public boolean hasSum() {
        return !Double.isNaN(sum);
    }

    /**
     * This will return garbage if {@link #hasCount()} is {@code false}.
     */
    public long getCount() {
        return count;
    }

    /**
     * This will return garbage if {@link #hasSum()} is {@code false}.
     */
    public double getSum() {
        return sum;
    }

    /**
     * May be {@link Exemplars#EMPTY}, but will never be {@code null}.
     */
    public Exemplars getExemplars() {
        return exemplars;
    }

    static abstract class Builder<T extends Builder<T>> extends DataPointSnapshot.Builder<T> {

        protected long count = -1;
        protected double sum = Double.NaN;
        protected long createdTimestampMillis = 0L;
        protected Exemplars exemplars = Exemplars.EMPTY;

        /**
         * Count can be explicitly set on summaries (this is a public method for summary metrics),
         * and it is set implicitly on histograms (derived from the bucket counts).
         */
        protected T count(long count) {
            this.count = count;
            return self();
        }

        public T sum(double sum) {
            this.sum = sum;
            return self();
        }

        public T exemplars(Exemplars exemplars) {
            this.exemplars = exemplars;
            return self();
        }

        public T createdTimestampMillis(long createdTimestampMillis) {
            this.createdTimestampMillis = createdTimestampMillis;
            return self();
        }
    }
}

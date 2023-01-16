package io.prometheus.metrics.model;

public abstract class DistributionMetricData extends MetricData {
    private final long count;
    private final double sum;
    private final Exemplars exemplars;

    protected DistributionMetricData(long count, double sum, Exemplars exemplars, Labels labels, long createdTimestampMillis, long timestampMillis) {
        super(labels, createdTimestampMillis, timestampMillis);
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
}

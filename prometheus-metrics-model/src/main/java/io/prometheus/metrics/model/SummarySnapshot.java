package io.prometheus.metrics.model;

public final class SummarySnapshot extends Snapshot {

    private final long count;
    private final double sum;
    private final Quantiles quantiles;
    private final long createdTimeMillis;

    public SummarySnapshot(long count, double sum, Quantiles quantiles, Labels labels, long createdTimeMillis) {
        super(labels);
        this.count = count;
        this.sum = sum;
        this.quantiles = quantiles;
        this.createdTimeMillis = createdTimeMillis;
    }

    public long getCount() {
        return count;
    }
    public double getSum() {
        return sum;
    }
    public Quantiles getQuantiles() {
        return quantiles;
    }
    public long getCreatedTimeMillis() {
        return createdTimeMillis;
    }
}

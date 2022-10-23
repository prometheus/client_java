package io.prometheus.metrics.model;

public abstract class SummarySnapshot extends Snapshot {

    public abstract long getCount();
    public abstract double getSum();
    public abstract Quantiles getQuantiles();
    public abstract long getCreatedTimeMillis();
}

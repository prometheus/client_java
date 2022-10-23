package io.prometheus.metrics.model;

import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends Snapshot {

    private final int schema;
    private final double zeroThreshold;
    private final List<ExponentialBucket> bucketsForPositiveValues;
    private final List<ExponentialBucket> bucketsForNegativeValues;

    public ExponentialBucketsHistogramSnapshot(int schema, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels) {
        this.schema = schema;
        this.zeroThreshold = zeroThreshold;
        this.bucketsForPositiveValues = bucketsForPositiveValues;
        this.bucketsForNegativeValues = bucketsForNegativeValues;
    }

    public double getZeroThreshold() {
        return zeroThreshold;
    }

    @Override
    public Labels getLabels() {
        return null;
    }
}

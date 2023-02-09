package io.prometheus.metrics.model;

public class FixedHistogramBucket implements Comparable<FixedHistogramBucket> {

    private final long cumulativeCount;
    private final double upperBound;

    public FixedHistogramBucket(double upperBound, long cumulativeCount) {
        this.cumulativeCount = cumulativeCount;
        this.upperBound = upperBound;
        if (Double.isNaN(upperBound)) {
            throw new IllegalArgumentException("Cannot use NaN as an upper bound for a histogram bucket");
        }
        if (cumulativeCount < 0) {
            throw new IllegalArgumentException(cumulativeCount + ": Histogram buckets cannot have a negative count");
        }
    }

    public long getCumulativeCount() {
        return cumulativeCount;
    }

    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public int compareTo(FixedHistogramBucket other) {
        return Double.compare(upperBound, other.upperBound);
    }
}

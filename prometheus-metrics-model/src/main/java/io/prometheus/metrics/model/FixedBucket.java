package io.prometheus.metrics.model;

public class FixedBucket implements Comparable<FixedBucket> {
    private final long cumulativeCount;
    private final double upperBound;
    private final Exemplar exemplar;

    public FixedBucket(long cumulativeCount, double upperBound) {
        this(cumulativeCount, upperBound, null);
    }

    public FixedBucket(long cumulativeCount, double upperBound, Exemplar exemplar) {
        this.cumulativeCount = cumulativeCount;
        this.upperBound = upperBound;
        this.exemplar = exemplar;
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

    public Exemplar getExemplar() {
        return exemplar;
    }

    @Override
    public int compareTo(FixedBucket other) {
        return Double.compare(upperBound, other.upperBound);
    }
}

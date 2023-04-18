package io.prometheus.metrics.model;

public class ClassicHistogramBucket implements Comparable<ClassicHistogramBucket> {

    private final long count;
    private final double upperBound;

    public ClassicHistogramBucket(double upperBound, long count) {
        this.count = count;
        this.upperBound = upperBound;
        if (Double.isNaN(upperBound)) {
            throw new IllegalArgumentException("Cannot use NaN as an upper bound for a histogram bucket");
        }
        if (count < 0) {
            throw new IllegalArgumentException(count + ": " + ClassicHistogramBuckets.class.getSimpleName() + " cannot have a negative count");
        }
    }

    public long getCount() {
        return count;
    }

    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public int compareTo(ClassicHistogramBucket other) {
        return Double.compare(upperBound, other.upperBound);
    }
}

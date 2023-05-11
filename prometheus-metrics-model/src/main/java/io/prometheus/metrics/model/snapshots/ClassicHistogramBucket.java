package io.prometheus.metrics.model.snapshots;

/**
 * Helper class for iterating over {@link ClassicHistogramBuckets}.
 * Note that the {@code count} is <i>not</i> cumulative.
 */
public class ClassicHistogramBucket implements Comparable<ClassicHistogramBucket> {

    private final long count; // not cumulative
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

    /**
     * For sorting a list of buckets by upper bound.
     */
    @Override
    public int compareTo(ClassicHistogramBucket other) {
        int result = Double.compare(upperBound, other.upperBound);
        if (result != 0) {
            return result;
        }
        return Long.compare(count, other.count);
    }
}

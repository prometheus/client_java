package io.prometheus.metrics.model;

public class ExplicitBucket {
    private final long cumulativeCount;
    private final double upperBound;
    private final Exemplar exemplar;

    public ExplicitBucket(long cumulativeCount, double upperBound, Exemplar exemplar) {
        this.cumulativeCount = cumulativeCount;
        this.upperBound = upperBound;
        this.exemplar = exemplar;
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
}

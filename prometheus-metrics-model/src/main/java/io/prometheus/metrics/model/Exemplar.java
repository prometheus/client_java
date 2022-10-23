package io.prometheus.metrics.model;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public class Exemplar {

    private final double value;
    private final Labels labels;
    private final Long timestampMillis;

    public Exemplar(double value, Labels labels, Long timestampMillis) {
        this.value = value;
        this.labels = labels;
        this.timestampMillis = timestampMillis;
    }

    public double getValue() {
        return value;
    }

    public Labels getLabels() {
        return labels;
    }

    /**
     * may be null
     */
    public Long getTimestampMillis() {
        return timestampMillis;
    }
}

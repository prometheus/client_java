package io.prometheus.metrics.model;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public class Exemplar {

    public static final String TRACE_ID = "trace_id"; // label name for trace id
    public static final String SPAN_ID = "span_id"; // label name for span id

    private final double value;
    private final Labels labels;
    private final Long timestampMillis;

    private Exemplar(double value, Labels labels, Long timestampMillis) {
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Double value = null;
        private Labels labels = Labels.EMPTY;
        private Long timestampMillis;

        private Builder() {}

        public Builder withValue(double value) {
            this.value = value;
            return this;
        }

        public Builder withTraceId(String traceId) {
            if (traceId == null) {
                throw new NullPointerException();
            }
            labels = labels.add(TRACE_ID, traceId);
            return this;
        }

        public Builder withSpanId(String spanId) {
            if (spanId == null) {
                throw new NullPointerException();
            }
            labels = labels.add(SPAN_ID, spanId);
            return this;
        }

        public Builder withLabels(Labels labels) {
            if (labels == null) {
                throw new NullPointerException();
            }
            this.labels = this.labels.merge(labels);
            return this;
        }

        public Builder withTimestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        public Exemplar build() {
            if (timestampMillis == null) {
                timestampMillis = System.currentTimeMillis();
            }
            if (value == null) {
                throw new IllegalStateException("cannot build an Exemplar without a value");
            }
            return new Exemplar(value, labels, timestampMillis);
        }
    }
}

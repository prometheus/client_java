package io.prometheus.metrics.model;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public class Exemplar {

    public static final String TRACE_ID = "trace_id"; // label name for trace id
    public static final String SPAN_ID = "span_id"; // label name for span id

    private final double value;
    private final Labels labels;
    private final long timestampMillis;

    /**
     * To create a new {@link Exemplar}, you can either call the constructor directly
     * or use the Builder with {@link Exemplar#newBuilder()}.
     * @param value the observed value
     * @param labels in most cases the labels will contain the {@link #TRACE_ID} and {@link #SPAN_ID}.
     *               Must not be {@code null}. Use {@link Labels#EMPTY} if no labels are present.
     * @param timestampMillis timestamp when the value was observed. Optional. Use 0L if not available.
     */
    public Exemplar(double value, Labels labels, long timestampMillis) {
        if (labels == null) {
            throw new NullPointerException("Labels cannot be null. Use Labels.EMPTY.");
        }
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

    public boolean hasTimestamp() {
        return timestampMillis != 0L;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Double value = null;
        private Labels labels = Labels.EMPTY;
        private String traceId = null;
        private String spanId = null;
        private long timestampMillis = 0L;

        private Builder() {}

        public Builder withValue(double value) {
            this.value = value;
            return this;
        }

        public Builder withTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder withSpanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder withLabels(Labels labels) {
            if (labels == null) {
                throw new NullPointerException();
            }
            this.labels = labels;
            return this;
        }

        public Builder withTimestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        /**
         * @throws IllegalStateException if {@link #withValue(double)} wasn't called.
         */
        public Exemplar build() {
            if (value == null) {
                throw new IllegalStateException("cannot build an Exemplar without a value");
            }
            Labels allLabels;
            if (traceId != null && spanId != null) {
                allLabels = Labels.of(TRACE_ID, traceId, SPAN_ID, spanId);
            } else if (traceId != null) {
                allLabels = Labels.of(TRACE_ID, traceId);
            } else if (spanId != null) {
                allLabels = Labels.of(SPAN_ID, spanId);
            } else {
                allLabels = Labels.EMPTY;
            }
            if (!labels.isEmpty()) {
                allLabels = allLabels.merge(labels);
            }
            return new Exemplar(value, allLabels, timestampMillis);
        }
    }
}

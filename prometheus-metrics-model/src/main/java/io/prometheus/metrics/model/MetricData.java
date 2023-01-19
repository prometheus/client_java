package io.prometheus.metrics.model;

public abstract class MetricData {
    private final Labels labels;
    private final long createdTimestampMillis;
    private final long timestampMillis;

    protected MetricData(Labels labels, long createdTimestampMillis, long timestampMillis) {
        this.labels = labels;
        this.createdTimestampMillis = createdTimestampMillis;
        this.timestampMillis = timestampMillis;
        if (timestampMillis != 0L && timestampMillis < createdTimestampMillis) {
            throw new IllegalArgumentException("The current timestamp cannot be before the created timestamp");
        }
    }

    public Labels getLabels() {
        return labels;
    }

    protected abstract void validate();

    public boolean hasTimestamp() {
        return timestampMillis != 0L;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    /**
     * Some metrics like Gauge don't have created timestamps, so for these metrics this will always return true.
     */
    public boolean hasCreatedTimestamp() {
        return createdTimestampMillis != 0L;
    }

    public long getCreatedTimestampMillis() {
        return createdTimestampMillis;
    }

    public static abstract class Builder<T extends Builder<T>> {

        private Labels labels = Labels.EMPTY;
        private long timestampMillis = 0L;

        public T withLabels(Labels labels) {
            this.labels = labels;
            return self();
        }

        /**
         * In most cases you should not need to set a timestamp, because the timestamp of a Prometheus metric should
         * usually be set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public T withTimestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return self();
        }


        protected long getTimestampMillis() {
            return timestampMillis;
        }
        protected Labels getLabels() {
            return labels;
        }

        protected abstract T self();
    }
}

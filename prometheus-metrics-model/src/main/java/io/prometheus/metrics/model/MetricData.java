package io.prometheus.metrics.model;

public abstract class MetricData {
    private final Labels labels;
    private final long createdTimestampMillis;
    private final long scrapeTimestampMillis;

    protected MetricData(Labels labels, long createdTimestampMillis, long scrapeTimestampMillis) {
        this.labels = labels;
        this.createdTimestampMillis = createdTimestampMillis;
        this.scrapeTimestampMillis = scrapeTimestampMillis;
        if (scrapeTimestampMillis != 0L && scrapeTimestampMillis < createdTimestampMillis) {
            throw new IllegalArgumentException("The scrape timestamp cannot be before the created timestamp");
        }
    }

    public Labels getLabels() {
        return labels;
    }

    protected abstract void validate();

    public boolean hasScrapeTimestamp() {
        return scrapeTimestampMillis != 0L;
    }

    public long getScrapeTimestampMillis() {
        return scrapeTimestampMillis;
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

        protected Labels labels = Labels.EMPTY;
        protected long scrapeTimestampMillis = 0L;

        public T withLabels(Labels labels) {
            this.labels = labels;
            return self();
        }

        /**
         * In most cases you should not need to set a timestamp, because the timestamp of a Prometheus metric should
         * usually be set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public T withScrapeTimestampMillis(long scrapeTimestampMillis) {
            this.scrapeTimestampMillis = scrapeTimestampMillis;
            return self();
        }

        protected abstract T self();
    }
}

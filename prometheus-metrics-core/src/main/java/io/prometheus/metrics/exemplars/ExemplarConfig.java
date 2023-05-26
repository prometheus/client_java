package io.prometheus.metrics.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import static java.util.stream.DoubleStream.concat;


/**
 * {@link ExemplarConfig} can be used to overwrite the {@link DefaultExemplarConfig}.
 */
public class ExemplarConfig {

    private final Object spanContextSupplier;
    private final Long minAgeMillis;
    private final Long maxAgeMillis;
    private final Long sampleIntervalMillis;
    private final Integer nExemplars;
    private final double[] upperBounds;

    private ExemplarConfig(Object spanContextSupplier, double[] upperBounds, Integer nExemplars,
                           Long minAgeMillis, Long maxAgeMillis, Long sampleIntervalMillis) {
        this.spanContextSupplier = spanContextSupplier;
        this.upperBounds = upperBounds; // upperBounds is already a sorted copy including +Inf.
        this.nExemplars = nExemplars;
        this.minAgeMillis = minAgeMillis;
        this.maxAgeMillis = maxAgeMillis;
        this.sampleIntervalMillis = sampleIntervalMillis;
    }

    public boolean hasSpanContextSupplier() {
        return spanContextSupplier != null || DefaultExemplarConfig.hasSpanContextSupplier();
    }


    public Object getSpanContextSupplier() {
        return spanContextSupplier != null ? spanContextSupplier : DefaultExemplarConfig.getSpanContextSupplier();
    }

    public long getMinAgeMillis() {
        return minAgeMillis != null ? minAgeMillis : DefaultExemplarConfig.getMinAgeMillis();
    }

    public long getMaxAgeMillis() {
        return maxAgeMillis != null ? maxAgeMillis : DefaultExemplarConfig.getMaxAgeMillis();
    }

    public long getSampleIntervalMillis() {
        return sampleIntervalMillis != null ? sampleIntervalMillis : DefaultExemplarConfig.getSampleIntervalMillis();
    }

    public Integer getNumberOfExemplars() {
        return nExemplars != null ? nExemplars : DefaultExemplarConfig.getNumberOfExemplars();
    }

    public double[] getUpperBounds() {
        return upperBounds;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder result = newBuilder();
        if (spanContextSupplier != null) {
            result.withSpanContextSupplier(spanContextSupplier);
        }
        if (nExemplars != null) {
            result.withNumberOfExemplars(nExemplars);
        }
        if (upperBounds != null) {
            result.withBuckets(upperBounds);
        }
        if (minAgeMillis != null) {
            result.withMinAge(minAgeMillis, TimeUnit.MILLISECONDS);
        }
        if (maxAgeMillis != null) {
            result.withMaxAge(maxAgeMillis, TimeUnit.MILLISECONDS);
        }
        if (sampleIntervalMillis != null) {
            result.withSampleInterval(sampleIntervalMillis, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    public static class Builder {

        private Object spanContextSupplier;
        private Integer nExemplars;
        private double[] upperBounds;
        private Long minAgeMillis;
        private Long maxAgeMillis;
        private Long sampleIntervalMillis;

        private Builder() {
        }

        /**
         * Set a custom {@link SpanContextSupplier}. Default is {@link ExemplarConfig#getSpanContextSupplier()}.
         *
         * @param spanContextSupplier must be an instance of {@link SpanContextSupplier}. The reason why the
         *                            type is {@link Object} is that users may remove the Exemplars dependencies
         *                            from the classpath.
         */
        public Builder withSpanContextSupplier(Object spanContextSupplier) {
            if (!(spanContextSupplier instanceof SpanContextSupplier)) {
                throw new IllegalArgumentException(spanContextSupplier.getClass().getSimpleName() + " does not implement SpanContextSupplier");
            }
            this.spanContextSupplier = spanContextSupplier;
            return this;
        }

        /**
         * Number of Exemplars. Default is 4.
         * <p>
         * If {@link #withBuckets(double...)} is set the number of Exemplars will be the number of buckets,
         * and {@link #withNumberOfExemplars(int)} will be ignored.
         */
        public Builder withNumberOfExemplars(int nExemplars) {
            if (nExemplars <= 0) {
                throw new IllegalArgumentException(nExemplars + ": nExemplars must be > 0");
            }
            this.nExemplars = nExemplars;
            return this;
        }

        public boolean hasNumberOfExemplars() {
            return nExemplars != null;
        }

        /**
         * In classic histograms there is one Exemplar per bucket.
         * <p>
         * Use {@code withBuckets(double...)} to create an {@link ExemplarConfig} for a classic histogram.
         */
        public Builder withBuckets(double... upperBounds) {
            this.upperBounds = concat(DoubleStream.of(upperBounds), DoubleStream.of(Double.POSITIVE_INFINITY))
                            .distinct()
                            .sorted()
                            .toArray();
            return this;
        }

        public boolean hasBuckets() {
            return upperBounds != null;
        }

        /**
         * Minimum retention time for Exemplars, i.e. Exemplars will be kept at least until the reach minAge.
         * <p>
         * Should be a bit less than the scrape interval. Default is 7 seconds.
         */
        public Builder withMinAge(long minAge, TimeUnit unit) {
            if (minAge <= 0) {
                throw new IllegalArgumentException(minAge + ": minAge must be >= 0");
            }
            this.minAgeMillis = unit.toMillis(minAge);
            if (maxAgeMillis != null) {
                if (maxAgeMillis < minAgeMillis) {
                    throw new IllegalArgumentException(minAge + ": minAge must be <= maxAge");
                }
            }
            return this;
        }

        /**
         * Maximum retention time for Exemplars: After maxAge Exemplars will be discarded.
         * <p>
         * Default is 70 seconds.
         */
        public Builder withMaxAge(long maxAge, TimeUnit unit) {
            if (maxAge <= 0) {
                throw new IllegalArgumentException(maxAge + ": maxAge must be > 0");
            }
            this.maxAgeMillis = unit.toMillis(maxAge);
            if (minAgeMillis != null) {
                if (minAgeMillis > maxAgeMillis) {
                    throw new IllegalArgumentException(maxAge + ": maxAge must be => minAge");
                }
            }
            return this;
        }

        /**
         * Sample interval. Default is 20 milliseconds.
         * <p>
         * The exemplar sampler is rate limited, i.e. Exemplars are updated at most every n milliseconds.
         */
        public Builder withSampleInterval(long sampleInterval, TimeUnit unit) {
            if (sampleInterval <= 0) {
                throw new IllegalArgumentException(sampleInterval + ": sampleInterval must be > 0");
            }
            this.sampleIntervalMillis = unit.toMillis(sampleInterval);
            return this;
        }

        public ExemplarConfig build() {
            return new ExemplarConfig(spanContextSupplier, upperBounds, nExemplars,
                    minAgeMillis, maxAgeMillis, sampleIntervalMillis);
        }

    }
}

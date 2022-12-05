package io.prometheus.metrics.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;

import java.util.stream.DoubleStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.DoubleStream.concat;

public class ExemplarConfig {

    private static volatile boolean exemplarsEnabled = true;
    private static volatile Object defaultSpanContextSupplier = findSpanContextSupplier();
    private static volatile long defaultMinAgeMillis = SECONDS.toMillis(7);
    private static volatile long defaultMaxAgeMillis = SECONDS.toMillis(70);
    private static volatile long defaultSampleIntervalMillis = 20;
    private static final int defaultNumberOfExemplars = 4; // cannot be changed, because exemplar samplers cannot dynamically change the number of exemplars
    public static void disableExemplars() {
        exemplarsEnabled = false;
    }

    public static void enableExemplars() {
        exemplarsEnabled = true;
    }

    private final Object spanContextSupplier;
    private final Long minAgeMillis;
    private final Long maxAgeMillis;
    private final Long sampleIntervalMillis;
    private final Integer nExemplars;
    private final double[] upperBounds;

    public static boolean isEnabled() {
        // TODO: Allow disabling via system property or environment variable
        return exemplarsEnabled;
    }

    public boolean hasSpanContextSupplier() {
        return spanContextSupplier != null || hasDefaultSpanContextSupplier();
    }

    public static boolean hasDefaultSpanContextSupplier() {
        return defaultSpanContextSupplier != null;
    }

    // Avoid SpanContextSupplier in the method signature so that we can handle the NoClassDefFoundError
    // even if the user excluded simpleclient_tracer_common from the classpath.
    public static void setDefaultSpanContextSupplier(Object spanContextSupplier) {
        if (!(spanContextSupplier instanceof SpanContextSupplier)) {
            // This will throw a NullPointerException if spanContextSupplier is null.
            throw new IllegalArgumentException(spanContextSupplier.getClass().getSimpleName() + " does not implement SpanContextSupplier");
        }
        defaultSpanContextSupplier = spanContextSupplier;
    }

    public static void setDefaultMinAgeMillis(long defaultMinAgeMillis) {
        ExemplarConfig.defaultMinAgeMillis = defaultMinAgeMillis;
    }

    public static void setDefaultMaxAgeMillis(long defaultMaxAgeMillis) {
        ExemplarConfig.defaultMaxAgeMillis = defaultMaxAgeMillis;
    }

    public static void setDefaultSampleIntervalMillis(long defaultSampleIntervalMillis) {
        ExemplarConfig.defaultSampleIntervalMillis = defaultSampleIntervalMillis;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private ExemplarConfig(Object spanContextSupplier, double[] upperBounds, Integer nExemplars,
                                  Long minAgeMillis, Long maxAgeMillis, Long sampleIntervalMillis) {
        this.spanContextSupplier = spanContextSupplier;
        this.upperBounds = upperBounds; // upperBounds is already a sorted copy including +Inf.
        this.nExemplars = nExemplars;
        this.minAgeMillis = minAgeMillis;
        this.maxAgeMillis = maxAgeMillis;
        this.sampleIntervalMillis = sampleIntervalMillis;
    }

    public Object getSpanContextSupplier() {
        return spanContextSupplier != null ? spanContextSupplier : defaultSpanContextSupplier;
    }

    public long getMinAgeMillis() {
        return minAgeMillis != null ? minAgeMillis : defaultMinAgeMillis;
    }

    public long getMaxAgeMillis() {
        return maxAgeMillis != null ? maxAgeMillis : defaultMaxAgeMillis;
    }

    public long getSampleIntervalMillis() {
        return sampleIntervalMillis != null ? sampleIntervalMillis : defaultSampleIntervalMillis;
    }

    public Integer getNumberOfExemplars() {
        return nExemplars != null ? nExemplars : defaultNumberOfExemplars;
    }

    public double[] getUpperBounds() {
        return upperBounds;
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
            result.withMinAgeMillis(minAgeMillis);
        }
        if (maxAgeMillis != null) {
            result.withMaxAgeMillis(maxAgeMillis);
        }
        if (sampleIntervalMillis != null) {
            result.withSampleIntervalMillis(sampleIntervalMillis);
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
         *                            type is {@link Object} is that users may remove the Exemplars dependencies from the classpath.
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
         * <p/>
         * You can either set the number of exemplars or buckets, but not both.
         */
        public Builder withNumberOfExemplars(int nExemplars) {
            if (nExemplars <= 0) {
                throw new IllegalArgumentException(nExemplars + ": nExemplars should be > 0");
            }
            if (upperBounds != null) {
                throw new IllegalArgumentException("Cannot set both nExemplars and buckets");
            }
            this.nExemplars = nExemplars;
            return this;
        }

        public boolean hasNumberOfExemplars() {
            return nExemplars != null;
        }

        public Builder withBuckets(double... upperBounds) {
            if (nExemplars != null) {
                throw new IllegalArgumentException("Cannot set both nExemplars and buckets");
            }
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
         * Minimum retention time for Exemplars.
         * <p>
         * Should be a bit less than the scrape interval. Default is 7000 milliseconds.
         */
        public Builder withMinAgeMillis(long minAgeMillis) {
            if (minAgeMillis <= 0) {
                throw new IllegalArgumentException(minAgeMillis + ": minAgeMillis should be >= 0");
            }
            if (maxAgeMillis != null) {
                if (maxAgeMillis < minAgeMillis) {
                    throw new IllegalArgumentException(minAgeMillis + ": minAgeMillis must be <= maxAgeMillis");
                }
            }
            this.minAgeMillis = minAgeMillis;
            return this;
        }

        /**
         * Maximum retention time for Exemplars.
         * <p>
         * The Exemplar with the smallest value is kept until a smaller one is observed or until maxAgeMillis is
         * reached. Likewise, the Exemplar with the largest value is kept until a larger one is observed or until
         * maxAgeMillis is reached. Default is 70_000 milliseconds.
         */
        public Builder withMaxAgeMillis(long maxAgeMillis) {
            if (maxAgeMillis <= 0) {
                throw new IllegalArgumentException(maxAgeMillis + ": maxAgeMillis must be > 0");
            }
            if (minAgeMillis != null) {
                if (minAgeMillis > maxAgeMillis) {
                    throw new IllegalArgumentException(maxAgeMillis + ": maxAgeMillis must be => minAgeMillis");
                }
            }
            this.maxAgeMillis = maxAgeMillis;
            return this;
        }

        /**
         * Sample interval.
         * <p>
         * The exemplar sampler is rate limited, i.e. Exemplars are updated at most every n milliseconds.
         * This is done for performance: The Exemplar Sampler is called for each counter update, and
         * you want this to be a NOOP for most of the calls.
         */
        public Builder withSampleIntervalMillis(long sampleIntervalMillis) {
            if (sampleIntervalMillis <= 0) {
                throw new IllegalArgumentException(sampleIntervalMillis + ": sampleIntervalMillis must be > 0");
            }
            this.sampleIntervalMillis = sampleIntervalMillis;
            return this;
        }

        public ExemplarConfig build() {
            return new ExemplarConfig(spanContextSupplier, upperBounds, nExemplars,
                    minAgeMillis, maxAgeMillis, sampleIntervalMillis);
        }

    }


    private static Object findSpanContextSupplier() {
        try {
            if (OpenTelemetrySpanContextSupplier.isAvailable()) {
                return new OpenTelemetrySpanContextSupplier();
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
        try {
            if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
                return new OpenTelemetryAgentSpanContextSupplier();
            }
        } catch (NoClassDefFoundError ignored) {
            // tracer_otel_agent dependency not found
        } catch (UnsupportedClassVersionError ignored) {
            // OpenTelemetry requires Java 8, but client_java might run on Java 6.
        }
        return null;
    }
}

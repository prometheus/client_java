package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counter metric.
 * <p>
 * Example usage:
 * <pre>{@code
 * Counter requestCount = Counter.newBuilder()
 *     .withName("requests_total")
 *     .withHelp("Total number of requests")
 *     .withLabelNames("path", "status")
 *     .register();
 * requestCount.withLabelValues("/hello-world", "200").inc();
 * requestCount.withLabelValues("/hello-world", "500").inc();
 * }</pre>
 */
public class Counter extends StatefulMetric<CounterDataPoint, Counter.DataPoint> implements CounterDataPoint, Collector {

    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;

    private Counter(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricProperties[] properties = getMetricProperties(builder, prometheusProperties);
        exemplarsEnabled = getConfigProperty(properties, MetricProperties::getExemplarsEnabled);
        if (exemplarsEnabled) {
            exemplarSamplerConfig = new ExemplarSamplerConfig(prometheusProperties.getExemplarConfig(), 1);
        } else {
            exemplarSamplerConfig = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inc(long amount) {
        getNoLabels().inc(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inc(double amount) {
        getNoLabels().inc(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incWithExemplar(long amount, Labels labels) {
        getNoLabels().incWithExemplar(amount, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incWithExemplar(double amount, Labels labels) {
        getNoLabels().incWithExemplar(amount, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterSnapshot collect() {
        return (CounterSnapshot) super.collect();
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return exemplarsEnabled;
    }

    @Override
    protected DataPoint newDataPoint() {
        if (isExemplarsEnabled()) {
            return new DataPoint(new ExemplarSampler(exemplarSamplerConfig));
        } else {
            return new DataPoint(null);
        }
    }

    @Override
    protected CounterSnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        List<CounterSnapshot.CounterDataPointSnapshot> data = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new CounterSnapshot(getMetadata(), data);
    }

    static String normalizeName(String name) {
        if (name != null && name.endsWith("_total")) {
            name = name.substring(0, name.length() - 6);
        }
        return name;
    }

    class DataPoint implements CounterDataPoint {

        private final DoubleAdder doubleValue = new DoubleAdder();
        // LongAdder is 20% faster than DoubleAdder. So let's use the LongAdder for long observations,
        // and DoubleAdder for double observations. If the user doesn't observe any double at all,
        // we will be using the LongAdder and get the best performance.
        private final LongAdder longValue = new LongAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final ExemplarSampler exemplarSampler; // null if isExemplarsEnabled() is false

        private DataPoint(ExemplarSampler exemplarSampler) {
            this.exemplarSampler = exemplarSampler;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inc(long amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observe(amount);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inc(double amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observe(amount);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void incWithExemplar(long amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void incWithExemplar(double amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        private void validateAndAdd(long amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            longValue.add(amount);
        }

        private void validateAndAdd(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            doubleValue.add(amount);
        }

        private CounterSnapshot.CounterDataPointSnapshot collect(Labels labels) {
            // Read the exemplar first. Otherwise, there is a race condition where you might
            // see an Exemplar for a value that's not counted yet.
            // If there are multiple Exemplars (by default it's just one), use the newest.
            Exemplar latestExemplar = null;
            if (exemplarSampler != null) {
                for (Exemplar exemplar : exemplarSampler.collect()) {
                    if (latestExemplar == null || exemplar.getTimestampMillis() > latestExemplar.getTimestampMillis()) {
                        latestExemplar = exemplar;
                    }
                }
            }
            return new CounterSnapshot.CounterDataPointSnapshot(longValue.sum() + doubleValue.sum(), labels, latestExemplar, createdTimeMillis);
        }
    }

    public static Builder newBuilder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder newBuilder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder extends StatefulMetric.Builder<Builder, Counter> {

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        /**
         * The {@code _total} suffix will automatically be appended if it's missing.
         * <pre>{@code
         * Counter c1 = Counter.newBuilder()
         *     .withName("events_total")
         *     .build();
         * Counter c2 = Counter.newBuilder()
         *     .withName("events")
         *     .build();
         * }</pre>
         * In the example above both {@code c1} and {@code c2} would be named {@code "events_total"} in Prometheus.
         * <p>
         * Throws an {@link IllegalArgumentException} if
         * {@link io.prometheus.metrics.model.snapshots.MetricMetadata#isValidMetricName(String) MetricMetadata.isValidMetricName(name)}
         * is {@code false}.
         */
        @Override
        public Builder withName(String name) {
            return super.withName(normalizeName(name));
        }

        @Override
        public Counter build() {
            return new Counter(this, properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

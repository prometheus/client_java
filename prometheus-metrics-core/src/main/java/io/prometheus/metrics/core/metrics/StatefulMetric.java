package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.core.datapoints.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * There are two kinds of metrics:
 * <ul>
 *     <li>A {@code StatefulMetric} actively maintains its current values, e.g. a stateful counter actively stores its current count.</li>
 *     <li>A {@code CallbackMetric} gets its values on demand when it is collected, e.g. a callback gauge representing the current heap size.</li>
 * </ul>
 * The OpenTelemetry terminology for <i>stateful</i> is <i>synchronous</i> and the OpenTelemetry terminology for <i>callback</i> is <i>asynchronous</i>.
 * We are using our own terminology here because in Java <i>synchronous</i> and <i>asynchronous</i> usually refers to multi-threading,
 * but this has nothing to do with multi-threading.
 */
abstract class StatefulMetric<D extends DataPoint, T extends D> extends MetricWithFixedMetadata {

    /**
     * Map label values to data points.
     */
    private final ConcurrentHashMap<List<String>, T> data = new ConcurrentHashMap<>();

    /**
     * Shortcut for data.get(Collections.emptyList())
     */
    private volatile T noLabels;

    protected StatefulMetric(Builder<?, ?> builder) {
        super(builder);
    }

    /**
     * labels and metricData have the same size. labels.get(i) are the labels for metricData.get(i).
     */
    protected abstract MetricSnapshot collect(List<Labels> labels, List<T> metricData);

    public MetricSnapshot collect() {
        if (labelNames.length == 0 && data.size() == 0) {
            // This is a metric without labels that has not been used yet. Initialize the data on the fly.
            labelValues();
        }
        List<Labels> labels = new ArrayList<>(data.size());
        List<T> metricData = new ArrayList<>(data.size());
        for (Map.Entry<List<String>, T> entry : data.entrySet()) {
            String[] labelValues = entry.getKey().toArray(new String[labelNames.length]);
            labels.add(constLabels.merge(labelNames, labelValues));
            metricData.add(entry.getValue());
        }
        return collect(labels, metricData);
    }

    /**
     * Initialize label values.
     * <p>
     * Example: Imagine you have a counter for payments as follows
     * <pre>
     * payment_transactions_total{payment_type="credit card"} 7.0
     * payment_transactions_total{payment_type="paypal"} 3.0
     * </pre>
     * Now, the data points for the {@code payment_type} label values get initialized when they are
     * first used, i.e. the first time you call
     * <pre>{@code
     * counter.labelValues("paypal").inc();
     * }</pre>
     * the data point with label {@code payment_type="paypal"} will go from non-existent to having value {@code 1.0}.
     * <p>
     * In some cases this is confusing, and you want to have data points initialized on application start
     * with an initial value of {@code 0.0}:
     * <pre>
     * payment_transactions_total{payment_type="credit card"} 0.0
     * payment_transactions_total{payment_type="paypal"} 0.0
     * </pre>
     * {@code initLabelValues(...)} can be used to initialize label value, so that the data points
     * show up in the exposition format with an initial value of zero.
     */
    public void initLabelValues(String... labelValues) {
        labelValues(labelValues);
    }

    public D labelValues(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            if (labelValues.length == 0) {
                throw new IllegalArgumentException(getClass().getSimpleName() + " " + getMetadata().getName() + " was created with label names, so you must call labelValues(...) when using it.");
            } else {
                throw new IllegalArgumentException("Expected " + labelNames.length + " label values, but got " + labelValues.length + ".");
            }
        }
        return data.computeIfAbsent(Arrays.asList(labelValues), l -> newDataPoint());
    }

    /**
     * Remove the data point with the given label values.
     * See <a href="https://prometheus.io/docs/instrumenting/writing_clientlibs/#labels">https://prometheus.io/docs/instrumenting/writing_clientlibs/#labels</a>.
     */
    public void remove(String... labelValues) {
        data.remove(Arrays.asList(labelValues));
    }

    /**
     * Reset the metric (remove all data points).
     */
    public void clear() {
        data.clear();
    }

    protected abstract T newDataPoint();

    protected T getNoLabels() {
        if (noLabels == null) {
            // Note that this will throw an IllegalArgumentException if labelNames is not empty.
            noLabels = (T) labelValues();
        }
        return noLabels;
    }

    protected MetricsProperties[] getMetricProperties(Builder builder, PrometheusProperties prometheusProperties) {
        String metricName = getMetadata().getName();
        if (prometheusProperties.getMetricProperties(metricName) != null) {
            return new MetricsProperties[]{
                    prometheusProperties.getMetricProperties(metricName), // highest precedence
                    builder.toProperties(), // second-highest precedence
                    prometheusProperties.getDefaultMetricProperties(), // third-highest precedence
                    builder.getDefaultProperties() // fallback
            };
        } else {
            return new MetricsProperties[]{
                    builder.toProperties(), // highest precedence
                    prometheusProperties.getDefaultMetricProperties(), // second-highest precedence
                    builder.getDefaultProperties() // fallback
            };
        }
    }

    protected <T> T getConfigProperty(MetricsProperties[] properties, Function<MetricsProperties, T> getter) {
        T result;
        for (MetricsProperties props : properties) {
            result = getter.apply(props);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("Missing default config. This is a bug in the Prometheus metrics core library.");
    }

    protected abstract boolean isExemplarsEnabled();

    static abstract class Builder<B extends Builder<B, M>, M extends StatefulMetric<?, ?>> extends MetricWithFixedMetadata.Builder<B, M> {

        protected Boolean exemplarsEnabled;

        protected Builder(List<String> illegalLabelNames, PrometheusProperties config) {
            super(illegalLabelNames, config);
        }

        /**
         * Allow Exemplars for this metric.
         */
        public B withExemplars() {
            this.exemplarsEnabled = TRUE;
            return self();
        }

        /**
         * Turn off Exemplars for this metric.
         */
        public B withoutExemplars() {
            this.exemplarsEnabled = FALSE;
            return self();
        }

        /**
         * Override if there are more properties than just exemplars enabled.
         */
        protected MetricsProperties toProperties() {
            return MetricsProperties.builder()
                    .exemplarsEnabled(exemplarsEnabled)
                    .build();
        }

        /**
         * Override if there are more properties than just exemplars enabled.
         */
        public MetricsProperties getDefaultProperties() {
            return MetricsProperties.builder()
                    .exemplarsEnabled(true)
                    .build();
        }
    }
}

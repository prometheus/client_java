package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricProperties;
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
            withLabelValues();
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

    public D withLabelValues(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            if (labelValues.length == 0) {
                throw new IllegalArgumentException("The " + getClass().getSimpleName() + " was created with label names, so you must call withLabelValues() when using it.");
            } else {
                throw new IllegalArgumentException("Expected " + labelNames.length + " label values, but got " + labelValues.length + ".");
            }
        }
        return data.computeIfAbsent(Arrays.asList(labelValues), l -> newDataPoint());
    }

    // TODO: Remove automatically if label values have not been used in a while?
    public void remove(String... labelValues) {
        data.remove(Arrays.asList(labelValues));
    }

    protected abstract T newDataPoint();

    protected T getNoLabels() {
        if (noLabels == null) {
            // Note that this will throw an IllegalArgumentException if labelNames is not empty.
            noLabels = (T) withLabelValues();
        }
        return noLabels;
    }

    protected MetricProperties[] getMetricProperties(Builder builder, PrometheusProperties prometheusProperties) {
        String metricName = getMetadata().getName();
        if (prometheusProperties.getMetricProperties(metricName) != null) {
            return new MetricProperties[]{
                    prometheusProperties.getMetricProperties(metricName), // highest precedence
                    builder.toProperties(), // second-highest precedence
                    prometheusProperties.getDefaultMetricProperties(), // third-highest precedence
                    builder.getDefaultProperties() // fallback
            };
        } else {
            return new MetricProperties[]{
                    builder.toProperties(), // highest precedence
                    prometheusProperties.getDefaultMetricProperties(), // second-highest precedence
                    builder.getDefaultProperties() // fallback
            };
        }
    }

    protected <T> T getConfigProperty(MetricProperties[] properties, Function<MetricProperties, T> getter) {
        T result;
        for (MetricProperties props : properties) {
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

        public B withExemplars() {
            this.exemplarsEnabled = TRUE;
            return self();
        }

        public B withoutExemplars() {
            this.exemplarsEnabled = FALSE;
            return self();
        }

        /**
         * Override if there are more properties than just exemplars enabled.
         */
        protected MetricProperties toProperties() {
            return MetricProperties.newBuilder()
                    .withExemplarsEnabled(exemplarsEnabled)
                    .build();
        }

        /**
         * Override if there are more properties than just exemplars enabled.
         */
        public MetricProperties getDefaultProperties() {
            return MetricProperties.newBuilder()
                    .withExemplarsEnabled(true)
                    .build();
        }
    }
}

package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Labels;

import java.util.List;

/**
 * There are two kinds of metrics: {@code StatefulMetric} and {@code CallbackMetric}.
 * <p>
 * See JavaDoc on {@link StatefulMetric} for more info.
 */
abstract class CallbackMetric extends MetricWithFixedMetadata {

    protected CallbackMetric(Builder<?, ?> builder) {
        super(builder);
    }

    protected Labels makeLabels(String... labelValues) {
        if (labelNames.length == 0) {
            if (labelValues != null && labelValues.length > 0) {
                throw new IllegalArgumentException("Cannot pass label values to a " + this.getClass().getSimpleName() + " that was created without label names.");
            }
            return constLabels;
        } else {
            if (labelValues == null) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " was created with label names, but the callback was called without label values.");
            }
            if (labelValues.length != labelNames.length) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " was created with " + labelNames.length + " label names, but the callback was called with " + labelValues.length + " label values.");
            }
            return constLabels.merge(Labels.of(labelNames, labelValues));
        }
    }

    static abstract class Builder<B extends Builder<B, M>, M extends CallbackMetric> extends MetricWithFixedMetadata.Builder<B, M> {

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            super(illegalLabelNames, properties);
        }
    }
}

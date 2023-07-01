package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Labels;

import java.util.List;

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

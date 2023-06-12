package io.prometheus.metrics.core.metrics;

public class TodoTest {

    // if a metric with labels is created but never used it has no data.
    // The registry's collect() method should skip those metrics to avoid illegal protobuf or text format.

    // callback versions of metrics

    // build() called with name == null

    // call inc() without labels, but the metric was created with labels

    // call inc() with labels, but the metric was created without labels

    // for performance: Use return value of withLabels() directly
}

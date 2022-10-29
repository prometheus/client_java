package io.prometheus.metrics.model;

import java.util.Collection;

public interface Metric {

    // See MetricMetadata in https://github.com/prometheus/prometheus/blob/main/prompb/types.proto

    // Note: The OpenMetrics exposition format would call this a MetricFamily. However, I think the name
    // Metric works better as an API. See REFACTORING.md
    String getName();
    String getUnit();
    String getHelp();
    MetricType getType();
    Collection<? extends Snapshot> snapshot();
}

package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;

import java.util.List;

class DoublePointDataImpl extends PointDataImpl implements DoublePointData {

    private final double value;

    public DoublePointDataImpl(double value, long startEpochNanos, long epochNanos, Attributes attributes, List<DoubleExemplarData> exemplars) {
        super(startEpochNanos, epochNanos, attributes, exemplars);
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }
}

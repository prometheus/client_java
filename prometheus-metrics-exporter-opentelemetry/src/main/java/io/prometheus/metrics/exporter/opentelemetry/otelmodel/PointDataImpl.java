package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.PointData;

import java.util.List;

abstract class PointDataImpl implements PointData {

    private final long startEpochNanos;
    private final long epochNanos;
    private final Attributes attributes;
    private final List<DoubleExemplarData> exemplars;

    PointDataImpl(long startEpochNanos, long epochNanos, Attributes attributes, List<DoubleExemplarData> exemplars) {
        this.startEpochNanos = startEpochNanos;
        this.epochNanos = epochNanos;
        this.attributes = attributes;
        this.exemplars = exemplars;
    }

    @Override
    public long getStartEpochNanos() {
        return startEpochNanos;
    }

    @Override
    public long getEpochNanos() {
        return epochNanos;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public List<DoubleExemplarData> getExemplars() {
        return exemplars;
    }
}

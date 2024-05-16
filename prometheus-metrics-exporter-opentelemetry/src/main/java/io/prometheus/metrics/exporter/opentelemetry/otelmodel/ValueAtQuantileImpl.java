package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ValueAtQuantile;

public class ValueAtQuantileImpl implements ValueAtQuantile {

    private final double quantile;
    private final double value;

    public ValueAtQuantileImpl(double quantile, double value) {
        this.quantile = quantile;
        this.value = value;
    }

    @Override
    public double getQuantile() {
        return quantile;
    }

    @Override
    public double getValue() {
        return value;
    }
}

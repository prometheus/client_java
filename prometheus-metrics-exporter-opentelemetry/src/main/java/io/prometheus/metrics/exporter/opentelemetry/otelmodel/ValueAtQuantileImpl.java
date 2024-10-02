package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;

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

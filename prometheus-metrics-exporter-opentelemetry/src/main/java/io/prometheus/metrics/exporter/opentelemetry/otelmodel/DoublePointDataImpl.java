package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import java.util.List;

class DoublePointDataImpl extends PointDataImpl implements DoublePointData {

  private final double value;

  public DoublePointDataImpl(
      double value,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars) {
    super(startEpochNanos, epochNanos, attributes, exemplars);
    this.value = value;
  }

  @Override
  public double getValue() {
    return value;
  }
}

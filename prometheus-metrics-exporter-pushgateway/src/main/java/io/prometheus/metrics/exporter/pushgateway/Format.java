package io.prometheus.metrics.exporter.pushgateway;

import io.prometheus.metrics.annotations.StableApi;

@StableApi
public enum Format {
  PROMETHEUS_PROTOBUF,
  PROMETHEUS_TEXT
}

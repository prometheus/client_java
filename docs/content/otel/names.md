---
title: Names
weight: 3
---

OpenTelemetry naming conventions are different from Prometheus naming conventions. The mapping from
OpenTelemetry metric names to Prometheus metric names is well defined in
OpenTelemetry's [Prometheus and OpenMetrics Compatibility](https://opentelemetry.io/docs/specs/otel/compatibility/prometheus_and_openmetrics/)
spec, and
the [OpenTelemetryExporter](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.html)
implements that specification.

The goal is, if you set up a pipeline as illustrated below, you will see the same
metric names in the Prometheus server as if you had exposed Prometheus metrics
directly.

![Image of a with the Prometheus client library pushing metrics to an OpenTelemetry collector][otel-pipeline]

The main steps when converting OpenTelemetry metric names to Prometheus metric names are:

- Escape illegal characters as described in [Unicode support]
- If the metric has a unit, append the unit to the metric name, like `_seconds`.
- If the metric type has a suffix, append it, like `_total` for counters.

## Dots in Metric and Label Names

OpenTelemetry defines not only a line protocol, but also _semantic conventions_, i.e. standardized
metric and label names. For example,
OpenTelemetry's [Semantic Conventions for HTTP Metrics](https://opentelemetry.io/docs/specs/otel/metrics/semantic_conventions/http-metrics/)
say that if you instrument an HTTP server with OpenTelemetry, you must have a histogram named
`http.server.duration`.

Most names defined in semantic conventions use dots.
Dots in metric and label names are now supported in the Prometheus Java client library as
described in [Unicode support].

[Unicode support]: {{< relref "../exporters/unicode.md" >}}
[otel-pipeline]: /client_java/images/otel-pipeline.png

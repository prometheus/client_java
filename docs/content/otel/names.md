---
title: Names
weight: 3
---

OpenTelemetry naming conventions are different from Prometheus naming conventions. The mapping from OpenTelemetry metric names to Prometheus metric names is well defined in OpenTelemetry's [Prometheus and OpenMetrics Compatibility](https://opentelemetry.io/docs/specs/otel/compatibility/prometheus_and_openmetrics/) spec, and the [OpenTelemetryExporter](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.html) implements that specification.

The goal is, if you set up a pipeline as illustrated below, you will see the same metric names in the Prometheus server as if you had exposed Prometheus metrics directly.

![Image of a with the Prometheus client library pushing metrics to an OpenTelemetry collector](/client_java/images/otel-pipeline.png)

The main steps when converting OpenTelemetry metric names to Prometheus metric names are:

* Replace dots with underscores.
* If the metric has a unit, append the unit to the metric name, like `_seconds`.
* If the metric type has a suffix, append it, like `_total` for counters.

Dots in Metric and Label Names
------------------------------

OpenTelemetry defines not only a line protocol, but also _semantic conventions_, i.e. standardized metric and label names. For example, OpenTelemetry's [Semantic Conventions for HTTP Metrics](https://opentelemetry.io/docs/specs/otel/metrics/semantic_conventions/http-metrics/) say that if you instrument an HTTP server with OpenTelemetry, you must have a histogram named `http.server.duration`.

Most names defined in semantic conventions use dots. In the Prometheus server, the dot is an illegal character (this might change in future versions of the Prometheus server).

The Prometheus Java client library allows dots, so that you can use metric names and label names as defined in OpenTelemetry's semantic conventions.
The dots will automatically be replaced with underscores if you expose metrics in Prometheus format, but you will see the original names with dots if you push your metrics in OpenTelemetry format.

That way, you can use OTel-compliant metric and label names today when instrumenting your application with the Prometheus Java client, and you are prepared in case your monitoring backend adds features in the future that require OTel-compliant instrumentation.

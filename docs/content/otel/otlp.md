---
title: OTLP
weight: 1
---

The Prometheus Java client library allows you to push metrics to an OpenTelemetry endpoint using the OTLP protocol.

![Image of a with the Prometheus client library pushing metrics to an OpenTelemetry collector](/client_java/images/otel-pipeline.png)

To implement this, you need to include `prometheus-metrics-exporter` as a dependency

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-exporter-opentelemetry:1.0.0'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-exporter-opentelemetry</artifactId>
    <version>1.0.0</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

Initialize the `OpenTelemetryExporter` in your Java code:

```java
OpenTelemetryExporter.builder()
    // optional: call configuration methods here
    .buildAndStart();
```

By default, the `OpenTelemetryExporter` will push metrics every 60 seconds to `localhost:4317` using `grpc` protocol. You can configure this in code using the [OpenTelemetryExporter.Builder](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html), or at runtime via [`io.prometheus.exporter.opentelemetry.*`](../../config/config/#exporter-opentelemetry-properties) properties.

In addition to the Prometheus Java client configuration, the exporter also recognizes standard OpenTelemetry configuration. For example, you can set the [OTEL_EXPORTER_OTLP_METRICS_ENDPOINT](https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_metrics_endpoint) environment variable to configure the endpoint. The Javadoc for [OpenTelemetryExporter.Builder](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html) shows which settings have corresponding OTel configuration. The intended use case is that if you attach the [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/) for tracing, and use the Prometheus Java client for metrics, it is sufficient to configure the OTel agent because the Prometheus library will pick up the same configuration.

The [examples/example-exporter-opentelemetry](https://github.com/prometheus/client_java/tree/main/examples/example-exporter-opentelemetry) folder has a docker compose with a complete end-to-end example, including a Java app, the OTel collector, and a Prometheus server.

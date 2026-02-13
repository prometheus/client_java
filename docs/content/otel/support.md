---
title: OTel Support
weight: 2
---

The `prometheus-metrics-otel-support` module bundles the
OpenTelemetry SDK and the Prometheus exporter into a single
POM dependency.

Use this module when you want to combine OpenTelemetry
instrumentations (e.g. JVM runtime metrics) with the
Prometheus Java client on one `/metrics` endpoint.

## Dependencies

{{< tabs "otel-support-deps" >}}
{{< tab "Gradle" >}}

```groovy
implementation 'io.prometheus:prometheus-metrics-otel-support:$version'
```

{{< /tab >}}
{{< tab "Maven" >}}

```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-otel-support</artifactId>
    <version>$version</version>
    <type>pom</type>
</dependency>
```

{{< /tab >}}
{{< /tabs >}}

This single dependency replaces:

- `io.opentelemetry:opentelemetry-sdk`
- `io.opentelemetry:opentelemetry-exporter-prometheus`

## Use Cases

See [JVM Runtime Metrics]({{< relref "jvm-runtime-metrics.md" >}})
for a concrete example of combining OTel JVM metrics with
the Prometheus Java client.

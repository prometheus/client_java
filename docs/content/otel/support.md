---
title: OTel Support
weight: 2
---

The `prometheus-metrics-otel-support` module bundles the
OpenTelemetry SDK and the Prometheus exporter into a single
POM dependency. It also imports the OTel instrumentation BOM
so that version management is handled automatically.

Use this module when you want to combine OpenTelemetry
instrumentations (e.g. JVM runtime metrics) with the
Prometheus Java client on one `/metrics` endpoint.

## Dependencies

{{< tabs "otel-support-deps" >}}
{{< tab "Gradle" >}}

```groovy
implementation platform('io.prometheus:prometheus-metrics-otel-support:$version')
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

It also imports the `opentelemetry-instrumentation-bom-alpha`
so you can add OTel instrumentation modules (like
`opentelemetry-runtime-telemetry-java8`) without specifying
their version.

## Use Cases

See [JVM Runtime Metrics]({{< relref "jvm-runtime-metrics.md" >}})
for a concrete example of combining OTel JVM metrics with
the Prometheus Java client.

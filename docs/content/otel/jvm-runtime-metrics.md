---
title: JVM Runtime Metrics
weight: 4
---

OpenTelemetry's
[runtime-telemetry](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry) <!-- editorconfig-checker-disable-line -->
module is an alternative to
[prometheus-metrics-instrumentation-jvm]({{< relref "../instrumentation/jvm.md" >}})
for users who want JVM metrics following OTel semantic conventions.

Key advantages:

- Metric names follow
  [OTel semantic conventions](https://opentelemetry.io/docs/specs/semconv/runtime/jvm-metrics/) <!-- editorconfig-checker-disable-line -->
- Java 17+ JFR support (context switches, network I/O,
  lock contention, memory allocation)
- Alignment with the broader OTel ecosystem

Since OpenTelemetry's `opentelemetry-exporter-prometheus`
already depends on this library's `PrometheusRegistry`,
no additional code is needed in this library — only the
OTel SDK wiring shown below.

## Dependencies

Use the [OTel Support]({{< relref "support.md" >}}) module
to pull in the OTel SDK and Prometheus exporter, then add
the runtime-telemetry instrumentation:

{{< tabs "jvm-runtime-deps" >}}
{{< tab "Gradle" >}}

```groovy
implementation 'io.prometheus:prometheus-metrics-otel-support:$version'

// Use opentelemetry-runtime-telemetry-java8 (Java 8+)
// or opentelemetry-runtime-telemetry-java17 (Java 17+, JFR-based)
implementation 'io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java8:2.24.0-alpha'
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

<!-- Pick ONE of the following -->
<!-- Java 8+ -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-runtime-telemetry-java8</artifactId>
    <version>2.24.0-alpha</version>
</dependency>
<!-- Java 17+ (adds JFR-based metrics) -->
<!--
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-runtime-telemetry-java17</artifactId>
    <version>2.24.0-alpha</version>
</dependency>
-->
```

{{< /tab >}}
{{< /tabs >}}

## Standalone Setup

If you **only** want OTel runtime metrics exposed as
Prometheus, without any Prometheus Java client metrics:

```java
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.instrumentation.runtimemetrics.java8.RuntimeMetrics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

PrometheusHttpServer prometheusServer =
    PrometheusHttpServer.builder()
        .setPort(9464)
        .build();

OpenTelemetrySdk openTelemetry =
    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .registerMetricReader(prometheusServer)
                .build())
        .build();

RuntimeMetrics runtimeMetrics =
    RuntimeMetrics.builder(openTelemetry).build();

// Close on shutdown to stop metric collection and server
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
  runtimeMetrics.close();
  prometheusServer.close();
}));

// Scrape at http://localhost:9464/metrics
```

## Combined with Prometheus Java Client Metrics

If you already have Prometheus Java client metrics and want to
add OTel runtime metrics to the **same** `/metrics`
endpoint, use `PrometheusMetricReader` to bridge OTel
metrics into a `PrometheusRegistry`:

```java
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.opentelemetry.exporter.prometheus.PrometheusMetricReader;
import io.opentelemetry.instrumentation.runtimemetrics.java8.RuntimeMetrics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

PrometheusRegistry registry =
    new PrometheusRegistry();

// Register Prometheus metrics as usual
Counter myCounter = Counter.builder()
    .name("my_requests_total")
    .register(registry);

// Bridge OTel metrics into the same registry
PrometheusMetricReader reader =
    PrometheusMetricReader.create();
registry.register(reader);

OpenTelemetrySdk openTelemetry =
    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .registerMetricReader(reader)
                .build())
        .build();

RuntimeMetrics runtimeMetrics =
    RuntimeMetrics.builder(openTelemetry).build();
Runtime.getRuntime()
    .addShutdownHook(new Thread(runtimeMetrics::close));

// Expose everything on one endpoint
HTTPServer.builder()
    .port(9400)
    .registry(registry)
    .buildAndStart();
```

The [examples/example-otel-jvm-runtime-metrics](https://github.com/prometheus/client_java/tree/main/examples/example-otel-jvm-runtime-metrics) <!-- editorconfig-checker-disable-line -->
directory has a complete runnable example.

## Configuration

The `RuntimeMetricsBuilder` supports two configuration
options:

### `captureGcCause()`

Adds a `jvm.gc.cause` attribute to the `jvm.gc.duration`
metric, indicating why the garbage collection occurred
(e.g. `G1 Evacuation Pause`, `System.gc()`):

```java
RuntimeMetrics.builder(openTelemetry)
    .captureGcCause()
    .build();
```

### `emitExperimentalTelemetry()`

Enables additional experimental metrics beyond the stable
set. These are not yet part of the OTel semantic conventions
and may change in future releases:

- Buffer pool metrics (direct and mapped byte buffers)
- Extended CPU metrics
- Extended memory pool metrics
- File descriptor metrics

```java
RuntimeMetrics.builder(openTelemetry)
    .emitExperimentalTelemetry()
    .build();
```

Both options can be combined:

```java
RuntimeMetrics.builder(openTelemetry)
    .captureGcCause()
    .emitExperimentalTelemetry()
    .build();
```

Selective per-metric registration is not supported by the
runtime-telemetry API — it is all-or-nothing with these
two toggles.

## Java 17 JFR Support

The `opentelemetry-runtime-telemetry-java17` variant adds
JFR-based metrics. You can selectively enable features:

```java
import io.opentelemetry.instrumentation.runtimemetrics.java17.JfrFeature;
import io.opentelemetry.instrumentation.runtimemetrics.java17.RuntimeMetrics;

RuntimeMetrics.builder(openTelemetry)
    .enableFeature(JfrFeature.BUFFER_METRICS)
    .enableFeature(JfrFeature.NETWORK_IO_METRICS)
    .enableFeature(JfrFeature.LOCK_METRICS)
    .enableFeature(JfrFeature.CONTEXT_SWITCH_METRICS)
    .build();
```

## Metric Names

OTel metric names are converted to Prometheus format by
the exporter. Examples:

| OTel name                    | Prometheus name                    |
| ---------------------------- | ---------------------------------- |
| `jvm.memory.used`            | `jvm_memory_used_bytes`            |
| `jvm.gc.duration`            | `jvm_gc_duration_seconds`          |
| `jvm.thread.count`           | `jvm_thread_count`                 |
| `jvm.class.loaded`           | `jvm_class_loaded`                 |
| `jvm.cpu.recent_utilization` | `jvm_cpu_recent_utilization_ratio` |

See [Names]({{< relref "names.md" >}}) for full details on
how OTel names map to Prometheus names.

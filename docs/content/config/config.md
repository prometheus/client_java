---
title: Config
weight: 1
---

{{< toc >}}

The Prometheus metrics library provides multiple options how to override configuration at runtime:

* Properties file
* System properties
* Environment variables

Example:

```
io.prometheus.exporter.httpServer.port = 9401
```

The property above changes the port for the [HTTPServer exporter]({{< relref "/exporters/httpserver.md" >}}) to _9401_.

* Properties file: Add the line above to the properties file.
* System properties: Use the command line parameter `-Dio.prometheus.exporter.httpServer.port=9401` when starting your application.
* Environment variable: `export IO_PROMETHEUS_EXPORTER_HTTPSERVER_PORT=9401`. The name of the environment variable is the uppercase property name with dots replaced with underscores.

Location of the Properties File
-------------------------------

The properties file is searched in the following locations:

* `/prometheus.properties` in the classpath. This is for bundling a properties file with your application.
* System property `-Dprometheus.config=/path/to/prometheus.properties`.
* Enironment variable `PROMETHEUS_CONFIG=/path/to/prometheus.properties`.

Metrics Properties
------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.metrics.exemplarsEnabled | [Counter.Builder.withExemplars()](/client_java/api/io/prometheus/metrics/core/metrics/Counter.Builder.html#withExemplars()) | (1) |
| io.prometheus.metrics.histogramNativeOnly | [Histogram.Builder.nativeOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeOnly()) | |
| io.prometheus.metrics.histogramClassicOnly | [Histogram.Builder.classicOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicOnly()) | |
| io.prometheus.metrics.histogramClassicUpperBounds | [Histogram.Builder.classicBuckets()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicBuckets(double...)) | (2) |
| io.prometheus.metrics.histogramNativeInitialSchema | [Histogram.Builder.nativeInitialSchema()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeInitialSchema(int)) | |
| io.prometheus.metrics.histogramNativeMinZeroThreshold | [Histogram.Builder.nativeMinZeroThreshold()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMinZeroThreshold(double)) | |
| io.prometheus.metrics.histogramNativeMaxZeroThreshold | [Histogram.Builder.nativeMaxZeroThreshold()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxZeroThreshold(double)) | |
| io.prometheus.metrics.histogramNativeMaxNumberOfBuckets | [Histogram.Builder.nativeMaxNumberOfBuckets()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxNumberOfBuckets(int)) | |
| io.prometheus.metrics.histogramNativeResetDurationSeconds | [Histogram.Builder.nativeResetDuration()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeResetDuration(long,java.util.concurrent.TimeUnit)) | |
| io.prometheus.metrics.summaryQuantiles | [Summary.Builder.quantile(double)](https://prometheus.github.io/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double)) | (3) |
| io.prometheus.metrics.summaryQuantileErrors | [Summary.Builder.quantile(double, double)](ihttps://prometheus.github.io/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double,double)) | (4) |
| io.prometheus.metrics.summaryMaxAgeSeconds | [Summary.Builder.maxAgeSeconds()](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#maxAgeSeconds(long)) | |
| io.prometheus.metrics.summaryNumberOfAgeBuckets | [Summary.Builder.numberOfAgeBuckets()](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#numberOfAgeBuckets(int)) | |

**Notes**

(1) _withExemplars()_ and _withoutExemplars()_ are available for all metric types, not just for counters<br>
(2) Comma-separated list. Example: `.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10`.<br>
(3) Comma-separated list. Example: `0.5, 0.95, 0.99`.<br>
(4) Comma-separated list. If specified, the list must have the same length as `io.prometheus.metrics.summaryQuantiles`. Example: `0.01, 0.005, 0.005`.

There's one special feature about metric properties: You can set a property for one specific metric only by specifying the metric name. Example: Let's say you have a histogram named `latency_seconds`.

```
io.prometheus.metrics.histogramClassicUpperBounds = 0.2, 0.4, 0.8, 1.0
```

The line above sets histogram buckets for all histograms. However:

```
io.prometheus.metrics.latency_seconds.histogramClassicUpperBounds = 0.2, 0.4, 0.8, 1.0
```

The line above sets histogram buckets only for the histogram named `latency_seconds`.

This works for all Metrics properties.

Exemplar Properties
-------------------

TODO

Exporter Properties
-------------------

TODO

Exporter Filter Properties
--------------------------

TODO

Exporter HTTPServer Properties
------------------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.exporter.httpServer.port | [HTTPServer.Builder.port()](/client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#port(int)) | |

Exporter OpenTelemetry Properties
---------------------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.exporter.opentelemetry.protocol | [OpenTelemetryExporter.Builder.protocol()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#protocol(java.lang.String)) | (1) |
| io.prometheus.exporter.opentelemetry.endpoint | [OpenTelemetryExporter.Builder.endpoint()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#endpoint(java.lang.String)) | |
| io.prometheus.exporter.opentelemetry.headers | [OpenTelemetryExporter.Builder.headers](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#header(java.lang.String,java.lang.String)) | (2) |
| io.prometheus.exporter.opentelemetry.intervalSeconds | [OpenTelemetryExporter.Builder.intervalSeconds()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#intervalSeconds(int)) | |
| io.prometheus.exporter.opentelemetry.timeoutSeconds | [OpenTelemetryExporter.Builder.timeoutSeconds()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#timeoutSeconds(int)) | |
| io.prometheus.exporter.opentelemetry.serviceName | [OpenTelemetryExporter.Builder.serviceName()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceName(java.lang.String)) | |
| io.prometheus.exporter.opentelemetry.serviceNamespace | [OpenTelemetryExporter.Builder.serviceNamespace()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceNamespace(java.lang.String)) | |
| io.prometheus.exporter.opentelemetry.serviceInstanceId | [OpenTelemetryExporter.Builder.serviceInstanceId()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceInstanceId(java.lang.String)) | |
| io.prometheus.exporter.opentelemetry.serviceVersion | [OpenTelemetryExporter.Builder.serviceVersion()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceVersion(java.lang.String)) | |
| io.prometheus.exporter.opentelemetry.resourceAttributes | [OpenTelemetryExporter.Builder.resourceAttributes()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#resourceAttribute(java.lang.String,java.lang.String)) | (3) |

(1) Protocol can be `grpc` or `http/protobuf`.<br>
(2) Format: `key1=value1,key2=value2`<br>
(3) Format: `key1=value1,key2=value2`

Many of these attributes can alternatively be configured via OpenTelemetry environment variables, like `OTEL_EXPORTER_OTLP_ENDPOINT`. The Prometheus metrics library has support for OpenTelemetry environment variables. See Javadoc for details.

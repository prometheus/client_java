---
title: Config
weight: 1
---

{{< toc >}}

The Prometheus metrics library provides multiple options how to override configuration at runtime:

* Properties file
* System properties

Future releases will add more options, like configuration via environment variables.

Example:

```
io.prometheus.exporter.httpServer.port = 9401
```

The property above changes the port for the [HTTPServer exporter]({{< relref "/exporters/httpserver.md" >}}) to _9401_.

* Properties file: Add the line above to the properties file.
* System properties: Use the command line parameter `-Dio.prometheus.exporter.httpServer.port=9401` when starting your application.

Location of the Properties File
-------------------------------

The properties file is searched in the following locations:

* `/prometheus.properties` in the classpath. This is for bundling a properties file with your application.
* System property `-Dprometheus.config=/path/to/prometheus.properties`.
* Environment variable `PROMETHEUS_CONFIG=/path/to/prometheus.properties`.

Metrics Properties
------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.metrics.exemplarsEnabled | [Counter.Builder.withExemplars()](/client_java/api/io/prometheus/metrics/core/metrics/Counter.Builder.html#withExemplars()) | (1) (2) |
| io.prometheus.metrics.histogramNativeOnly | [Histogram.Builder.nativeOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeOnly()) | (2) |
| io.prometheus.metrics.histogramClassicOnly | [Histogram.Builder.classicOnly()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicOnly()) | (2) |
| io.prometheus.metrics.histogramClassicUpperBounds | [Histogram.Builder.classicUpperBounds()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicUpperBounds(double...)) | (3) |
| io.prometheus.metrics.histogramNativeInitialSchema | [Histogram.Builder.nativeInitialSchema()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeInitialSchema(int)) | |
| io.prometheus.metrics.histogramNativeMinZeroThreshold | [Histogram.Builder.nativeMinZeroThreshold()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMinZeroThreshold(double)) | |
| io.prometheus.metrics.histogramNativeMaxZeroThreshold | [Histogram.Builder.nativeMaxZeroThreshold()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxZeroThreshold(double)) | |
| io.prometheus.metrics.histogramNativeMaxNumberOfBuckets | [Histogram.Builder.nativeMaxNumberOfBuckets()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxNumberOfBuckets(int)) | |
| io.prometheus.metrics.histogramNativeResetDurationSeconds | [Histogram.Builder.nativeResetDuration()](/client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeResetDuration(long,java.util.concurrent.TimeUnit)) | |
| io.prometheus.metrics.summaryQuantiles | [Summary.Builder.quantile(double)](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double)) | (4) |
| io.prometheus.metrics.summaryQuantileErrors | [Summary.Builder.quantile(double, double)](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double,double)) | (5) |
| io.prometheus.metrics.summaryMaxAgeSeconds | [Summary.Builder.maxAgeSeconds()](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#maxAgeSeconds(long)) | |
| io.prometheus.metrics.summaryNumberOfAgeBuckets | [Summary.Builder.numberOfAgeBuckets()](/client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#numberOfAgeBuckets(int)) | |

**Notes**

(1) _withExemplars()_ and _withoutExemplars()_ are available for all metric types, not just for counters<br>
(2) Boolean value. Format: `property=true` or `property=false`.<br>
(3) Comma-separated list. Example: `.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10`.<br>
(4) Comma-separated list. Example: `0.5, 0.95, 0.99`.<br>
(5) Comma-separated list. If specified, the list must have the same length as `io.prometheus.metrics.summaryQuantiles`. Example: `0.01, 0.005, 0.005`.

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

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.exemplars.minRetentionPeriodSeconds  | [ExemplarsProperties.getMinRetentionPeriodSeconds()](/client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getMinRetentionPeriodSeconds()) | |
| io.prometheus.exemplars.maxRetentionPeriodSeconds  | [ExemplarsProperties.getMaxRetentionPeriodSeconds()](/client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getMaxRetentionPeriodSeconds()) | |
| io.prometheus.exemplars.sampleIntervalMilliseconds | [ExemplarsProperties.getSampleIntervalMilliseconds()](/client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getSampleIntervalMilliseconds()) | |

Exporter Properties
-------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.exporter.includeCreatedTimestamps  | [ExporterProperties.getIncludeCreatedTimestamps()](/client_java/api/io/prometheus/metrics/config/ExporterProperties.html#getIncludeCreatedTimestamps()) | (1) |
| io.prometheus.exporter.exemplarsOnAllMetricTypes  | [ExporterProperties.getExemplarsOnAllMetricTypes()](/client_java/api/io/prometheus/metrics/config/ExporterProperties.html#getExemplarsOnAllMetricTypes()) | (1) |

(1) Boolean value, `true` or `false`. Default see Javadoc.

Exporter Filter Properties
--------------------------

| Name            | Javadoc | Note |
| --------------- | --------|------|
| io.prometheus.exporter.filter.metricNameMustBeEqualTo | [ExporterFilterProperties.getAllowedMetricNames()](/client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getAllowedMetricNames()) | (1) |
| io.prometheus.exporter.filter.metricNameMustNotBeEqualTo | [ExporterFilterProperties.getExcludedMetricNames()](/client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getExcludedMetricNames()) | (2) |
| io.prometheus.exporter.filter.metricNameMustStartWith | [ExporterFilterProperties.getAllowedMetricNamePrefixes()](/client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getAllowedMetricNamePrefixes()) | (3) |
| io.prometheus.exporter.filter.metricNameMustNotStartWith | [ExporterFilterProperties.getExcludedMetricNamePrefixes()](/client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getExcludedMetricNamePrefixes()) | (4) |

(1) Comma sparated list of allowed metric names. Only these metrics will be exposed.<br/>
(2) Comma sparated list of excluded metric names. These metrics will not be exposed.<br/>
(3) Comma sparated list of prefixes. Only metrics starting with these prefixes will be exposed.<br/>
(4) Comma sparated list of prefixes. Metrics starting with these prefixes will not be exposed.<br/>

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
| io.prometheus.exporter.opentelemetry.headers | [OpenTelemetryExporter.Builder.headers()](/client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#header(java.lang.String,java.lang.String)) | (2) |
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

Exporter PushGateway Properties
-------------------------------

| Name                                       | Javadoc                                                                                                                                         | Note |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|------|
| io.prometheus.exporter.pushgateway.address | [PushGateway.Builder.address()](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#address(java.lang.String)) | |
| io.prometheus.exporter.pushgateway.scheme  | [PushGateway.Builder.scheme()](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#scheme(java.lang.String))   | |
| io.prometheus.exporter.pushgateway.job     | [PushGateway.Builder.job()](/client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#job(java.lang.String))         | |


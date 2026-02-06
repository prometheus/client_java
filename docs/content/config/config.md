---
title: Config
weight: 1
---

{{< toc >}}

The Prometheus metrics library provides multiple options how to override configuration at runtime:

- Properties file
- System properties
- Environment variables

Example:

```properties
io.prometheus.exporter.http_server.port=9401
```

The property above changes the port for the
[HTTPServer exporter]({{< relref "/exporters/httpserver.md" >}}) to _9401_.

- **Properties file**: Add the line above to the properties file.
- **System properties**: Use the command line parameter
  `-Dio.prometheus.exporter.http_server.port=9401` when starting your application.
- **Environment variables**: Set `IO_PROMETHEUS_EXPORTER_HTTP_SERVER_PORT=9401`

## Location of the Properties File

The properties file is searched in the following locations:

- `/prometheus.properties` in the classpath. This is for bundling a properties file
  with your application.
- System property `-Dprometheus.config=/path/to/prometheus.properties`.
- Environment variable `PROMETHEUS_CONFIG=/path/to/prometheus.properties`.

## Property Naming Conventions

Properties use **snake_case** format with underscores separating words
(e.g., `http_server`, `exemplars_enabled`).

For backward compatibility, camelCase property names are also supported in
properties files and system properties, but snake_case is the preferred format.

### Environment Variables

Environment variables follow standard conventions:

- All uppercase letters: `IO_PROMETHEUS_EXPORTER_HTTP_SERVER_PORT`
- Underscores for all separators (both package and word boundaries)
- Prefix must be `IO_PROMETHEUS`

The library automatically converts environment variables to the correct property format.

**Examples:**

| Environment Variable                          | Property Equivalent                           |
| --------------------------------------------- | --------------------------------------------- |
| `IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED`     | `io.prometheus.metrics.exemplars_enabled`     |
| `IO_PROMETHEUS_EXPORTER_HTTP_SERVER_PORT`     | `io.prometheus.exporter.http_server.port`     |
| `IO_PROMETHEUS_METRICS_HISTOGRAM_NATIVE_ONLY` | `io.prometheus.metrics.histogram_native_only` |

### Property Precedence

When the same property is defined in multiple sources, the following precedence order applies
(highest to lowest):

1. **External properties** (passed explicitly via API)
2. **Environment variables**
3. **System properties** (command line `-D` flags)
4. **Properties file** (from file or classpath)

## Metrics Properties

<!-- editorconfig-checker-disable -->

| Name                                                          | Javadoc                                                                                                                                                                         | Note    |
|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| io.prometheus.metrics.exemplars_enabled                       | [Counter.Builder.withExemplars()](</client_java/api/io/prometheus/metrics/core/metrics/Counter.Builder.html#withExemplars()>)                                                   | (1) (2) |
| io.prometheus.metrics.histogram_native_only                   | [Histogram.Builder.nativeOnly()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeOnly()>)                                                     | (2)     |
| io.prometheus.metrics.histogram_classic_only                  | [Histogram.Builder.classicOnly()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicOnly()>)                                                   | (2)     |
| io.prometheus.metrics.histogram_classic_upper_bounds          | [Histogram.Builder.classicUpperBounds()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#classicUpperBounds(double...)>)                            | (3)     |
| io.prometheus.metrics.histogram_native_initial_schema         | [Histogram.Builder.nativeInitialSchema()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeInitialSchema(int)>)                                |         |
| io.prometheus.metrics.histogram_native_min_zero_threshold     | [Histogram.Builder.nativeMinZeroThreshold()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMinZeroThreshold(double)>)                       |         |
| io.prometheus.metrics.histogram_native_max_zero_threshold     | [Histogram.Builder.nativeMaxZeroThreshold()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxZeroThreshold(double)>)                       |         |
| io.prometheus.metrics.histogram_native_max_number_of_buckets  | [Histogram.Builder.nativeMaxNumberOfBuckets()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeMaxNumberOfBuckets(int)>)                      |         |
| io.prometheus.metrics.histogram_native_reset_duration_seconds | [Histogram.Builder.nativeResetDuration()](</client_java/api/io/prometheus/metrics/core/metrics/Histogram.Builder.html#nativeResetDuration(long,java.util.concurrent.TimeUnit)>) |         |
| io.prometheus.metrics.summary_quantiles                       | [Summary.Builder.quantile(double)](</client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double)>)                                                 | (4)     |
| io.prometheus.metrics.summary_quantile_errors                 | [Summary.Builder.quantile(double, double)](</client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#quantile(double,double)>)                                  | (5)     |
| io.prometheus.metrics.summary_max_age_seconds                 | [Summary.Builder.maxAgeSeconds()](</client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#maxAgeSeconds(long)>)                                               |         |
| io.prometheus.metrics.summary_number_of_age_buckets           | [Summary.Builder.numberOfAgeBuckets()](</client_java/api/io/prometheus/metrics/core/metrics/Summary.Builder.html#numberOfAgeBuckets(int)>)                                      |         |
| io.prometheus.metrics.use_otel_metrics                        | [MetricsProperties.useOtelMetrics()](</client_java/api/io/prometheus/metrics/config/MetricsProperties.html#useOtelMetrics()>)                                                   | (2)     |


<!-- editorconfig-checker-enable -->

**Notes**

(1) _withExemplars()_ and _withoutExemplars()_ are available for all metric types,
not just for counters<br>
(2) Boolean value. Format: `property=true` or `property=false`.<br>
(3) Comma-separated list. Example: `.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10`.<br>
(4) Comma-separated list. Example: `0.5, 0.95, 0.99`.<br>
(5) Comma-separated list. If specified, the list must have the same length as
`io.prometheus.metrics.summary_quantiles`. Example: `0.01, 0.005, 0.005`.

There's one special feature about metric properties: You can set a property for one specific
metric only by specifying the metric name. Example:
Let's say you have a histogram named `latency_seconds`.

```properties
io.prometheus.metrics.histogram_classic_upper_bounds=0.2, 0.4, 0.8, 1.0
```

The line above sets histogram buckets for all histograms. However:

```properties
io.prometheus.metrics.latency_seconds.histogram_classic_upper_bounds=0.2, 0.4, 0.8, 1.0
```

The line above sets histogram buckets only for the histogram named `latency_seconds`.

This works for all Metrics properties.

## Exemplar Properties

<!-- editorconfig-checker-disable -->

| Name                                                 | Javadoc                                                                                                                                                         | Note |
| ---------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---- |
| io.prometheus.exemplars.min_retention_period_seconds | [ExemplarsProperties.getMinRetentionPeriodSeconds()](</client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getMinRetentionPeriodSeconds()>)   |      |
| io.prometheus.exemplars.max_retention_period_seconds | [ExemplarsProperties.getMaxRetentionPeriodSeconds()](</client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getMaxRetentionPeriodSeconds()>)   |      |
| io.prometheus.exemplars.sample_interval_milliseconds | [ExemplarsProperties.getSampleIntervalMilliseconds()](</client_java/api/io/prometheus/metrics/config/ExemplarsProperties.html#getSampleIntervalMilliseconds()>) |      |

<!-- editorconfig-checker-enable -->

## Exporter Properties

<!-- editorconfig-checker-disable -->

| Name                                                 | Javadoc                                                                                                                                                     | Note |
| ---------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ---- |
| io.prometheus.exporter.include_created_timestamps    | [ExporterProperties.getIncludeCreatedTimestamps()](</client_java/api/io/prometheus/metrics/config/ExporterProperties.html#getIncludeCreatedTimestamps()>)   | (1)  |
| io.prometheus.exporter.exemplars_on_all_metric_types | [ExporterProperties.getExemplarsOnAllMetricTypes()](</client_java/api/io/prometheus/metrics/config/ExporterProperties.html#getExemplarsOnAllMetricTypes()>) | (1)  |

<!-- editorconfig-checker-enable -->

(1) Boolean value, `true` or `false`. Default see Javadoc.

## Exporter Filter Properties

<!-- editorconfig-checker-disable -->

| Name                                                           | Javadoc                                                                                                                                                                   | Note |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---- |
| io.prometheus.exporter.filter.metric_name_must_be_equal_to     | [ExporterFilterProperties.getAllowedMetricNames()](</client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getAllowedMetricNames()>)                 | (1)  |
| io.prometheus.exporter.filter.metric_name_must_not_be_equal_to | [ExporterFilterProperties.getExcludedMetricNames()](</client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getExcludedMetricNames()>)               | (2)  |
| io.prometheus.exporter.filter.metric_name_must_start_with      | [ExporterFilterProperties.getAllowedMetricNamePrefixes()](</client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getAllowedMetricNamePrefixes()>)   | (3)  |
| io.prometheus.exporter.filter.metric_name_must_not_start_with  | [ExporterFilterProperties.getExcludedMetricNamePrefixes()](</client_java/api/io/prometheus/metrics/config/ExporterFilterProperties.html#getExcludedMetricNamePrefixes()>) | (4)  |

<!-- editorconfig-checker-enable -->

(1) Comma separated list of allowed metric names. Only these metrics will be exposed.<br/>
(2) Comma separated list of excluded metric names. These metrics will not be exposed.<br/>
(3) Comma separated list of prefixes.
Only metrics starting with these prefixes will be exposed.<br/>
(4) Comma separated list of prefixes. Metrics starting with these prefixes will not be exposed.<br/>

## Exporter HTTPServer Properties

<!-- editorconfig-checker-disable -->

| Name                                    | Javadoc                                                                                                                     | Note |
| --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ---- |
| io.prometheus.exporter.http_server.port | [HTTPServer.Builder.port()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#port(int)>) |      |

<!-- editorconfig-checker-enable -->

## Exporter OpenTelemetry Properties

<!-- editorconfig-checker-disable -->

| Name                                                     | Javadoc                                                                                                                                                                                                       | Note |
| -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---- |
| io.prometheus.exporter.opentelemetry.protocol            | [OpenTelemetryExporter.Builder.protocol()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#protocol(java.lang.String)>)                                     | (1)  |
| io.prometheus.exporter.opentelemetry.endpoint            | [OpenTelemetryExporter.Builder.endpoint()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#endpoint(java.lang.String)>)                                     |      |
| io.prometheus.exporter.opentelemetry.headers             | [OpenTelemetryExporter.Builder.headers()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#header(java.lang.String,java.lang.String)>)                       | (2)  |
| io.prometheus.exporter.opentelemetry.interval_seconds    | [OpenTelemetryExporter.Builder.intervalSeconds()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#intervalSeconds(int)>)                                    |      |
| io.prometheus.exporter.opentelemetry.timeout_seconds     | [OpenTelemetryExporter.Builder.timeoutSeconds()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#timeoutSeconds(int)>)                                      |      |
| io.prometheus.exporter.opentelemetry.service_name        | [OpenTelemetryExporter.Builder.serviceName()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceName(java.lang.String)>)                               |      |
| io.prometheus.exporter.opentelemetry.service_namespace   | [OpenTelemetryExporter.Builder.serviceNamespace()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceNamespace(java.lang.String)>)                     |      |
| io.prometheus.exporter.opentelemetry.service_instance_id | [OpenTelemetryExporter.Builder.serviceInstanceId()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceInstanceId(java.lang.String)>)                   |      |
| io.prometheus.exporter.opentelemetry.service_version     | [OpenTelemetryExporter.Builder.serviceVersion()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#serviceVersion(java.lang.String)>)                         |      |
| io.prometheus.exporter.opentelemetry.resource_attributes | [OpenTelemetryExporter.Builder.resourceAttributes()](</client_java/api/io/prometheus/metrics/exporter/opentelemetry/OpenTelemetryExporter.Builder.html#resourceAttribute(java.lang.String,java.lang.String)>) | (3)  |

<!-- editorconfig-checker-enable -->

(1) Protocol can be `grpc` or `http/protobuf`.<br>
(2) Format: `key1=value1,key2=value2`<br>
(3) Format: `key1=value1,key2=value2`

Many of these attributes can alternatively be configured via OpenTelemetry environment variables,
like `OTEL_EXPORTER_OTLP_ENDPOINT`.
The Prometheus metrics library has support for OpenTelemetry environment variables.
See Javadoc for details.

## Exporter PushGateway Properties

<!-- editorconfig-checker-disable -->

| Name                                               | Javadoc                                                                                                                                                                                    | Note |
| -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---- |
| io.prometheus.exporter.pushgateway.address         | [PushGateway.Builder.address()](</client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#address(java.lang.String)>)                                          |      |
| io.prometheus.exporter.pushgateway.scheme          | [PushGateway.Builder.scheme()](</client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#scheme(java.lang.String)>)                                            |      |
| io.prometheus.exporter.pushgateway.job             | [PushGateway.Builder.job()](</client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#job(java.lang.String)>)                                                  |      |
| io.prometheus.exporter.pushgateway.escaping_scheme | [PushGateway.Builder.escapingScheme()](</client_java/api/io/prometheus/metrics/exporter/pushgateway/PushGateway.Builder.html#escapingScheme(io.prometheus.metrics.config.EscapingScheme)>) | (1)  |

<!-- editorconfig-checker-enable -->

(1) Escaping scheme can be `allow-utf-8`, `underscores`, `dots`, or `values` as described in
[escaping schemes](https://github.com/prometheus/docs/blob/main/docs/instrumenting/escaping_schemes.md#escaping-schemes) <!-- editorconfig-checker-disable-line -->
and in the [Unicode documentation]({{< relref "../exporters/unicode.md" >}}).

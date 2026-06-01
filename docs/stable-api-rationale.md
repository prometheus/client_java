# Stable API rationale

This file records the first-pass guess for the `@StableApi` surface.

The guess is intentionally conservative about user-facing entry points, but it also includes
supporting public value types when they appear in signatures of those entry points. The
annotation is currently applied to top-level types. Public nested builders and data point
snapshots are treated as part of the annotated enclosing type.

Evidence labels:

- D-core: documented in getting-started metric, label, callback, registry, or
  multi-target docs.
- D-config: documented in `docs/content/config/config.md`.
- D-exporter: documented in exporter docs under `docs/content/exporters/`.
- D-instr: documented in instrumentation docs under `docs/content/instrumentation/`.
- D-migration: documented in `docs/content/migration/simpleclient.md`.
- D-otel: documented in OpenTelemetry docs under `docs/content/otel/`.
- M: imported or used by Micrometer's Prometheus registry or samples.
- J: imported or used by Prometheus JMX Exporter's collector, common, agent, or tests.
- S: support type in the public signatures or returned snapshots of another stable API.
- T: tooling contract for the API diff mechanism itself.

Entries justified only by S are the weakest guesses. They are included because excluding them
would make a documented or externally used API hard to use without unstable public types.

## Annotation module

### `io.prometheus.metrics.annotations`

- `StableApi`: T. This is the opt-in marker used by japicmp and by humans to decide
  which public types are part of the published compatibility contract.

## Config

### `io.prometheus.metrics.config`

- `EscapingScheme`: D-config, D-exporter. PushGateway and unicode docs expose escaping
  selection, and the type is accepted by documented builder methods.
- `ExemplarsProperties`: D-config, M. Micrometer constructs exemplar sampler config from
  this type, and the config docs link its getters directly.
- `ExporterFilterProperties`: D-config. Exporter filtering is documented as runtime
  configuration and maps to this public properties type.
- `ExporterHttpServerProperties`: D-config. The HTTP server exporter settings are
  documented and map to this public properties type.
- `ExporterOpenTelemetryProperties`: D-config, D-otel. OTLP exporter settings are
  documented and map to this public properties type.
- `ExporterProperties`: D-config. Exporter-wide options such as exemplar behavior are
  documented and exposed through this public type.
- `ExporterPushgatewayProperties`: D-config, D-exporter. PushGateway runtime settings are
  documented and map to this public properties type.
- `MetricsProperties`: D-config. Core metric defaults such as histogram and summary
  options are documented and exposed through this type.
- `OpenMetrics2Properties`: D-config, D-exporter. OM2 is experimental, but the public
  docs link this type and its getters explicitly.
- `PrometheusProperties`: D-config, D-exporter, D-otel, M, J. It is the root config type
  used by docs, Micrometer, and JMX Exporter.
- `PrometheusPropertiesException`: S. Public exception thrown by the public config loader
  and builder path; callers may catch it.
- `PrometheusPropertiesLoader`: D-config, M. The config docs describe loading runtime
  configuration, and Micrometer imports this loader.

## Core datapoints and timers

### `io.prometheus.metrics.core.datapoints`

- `CounterDataPoint`: S. Returned by documented `Counter` label accessors and callback
  APIs, so users can increment labeled counters.
- `DataPoint`: S. Common public supertype for metric child handles returned by stable
  metric APIs.
- `DistributionDataPoint`: S. Common public supertype behind histogram and summary child
  handles, including observe and exemplar operations.
- `GaugeDataPoint`: S. Returned by documented `Gauge` label accessors and callback APIs,
  so users can set and adjust labeled gauges.
- `StateSetDataPoint`: S. Returned by documented `StateSet` APIs, so users can set named
  state values.
- `Timer`: D-core, S. Returned by documented timer helpers used to time histogram and
  summary observations.
- `TimerApi`: D-core. The metric type docs link this interface for timing APIs on
  histogram and summary metrics.

## Core exemplars

### `io.prometheus.metrics.core.exemplars`

- `ExemplarSampler`: M, D-otel. Micrometer imports it for exemplar sampling, and the OTel
  tracing docs describe exemplar behavior as an integration surface.
- `ExemplarSamplerConfig`: M. Micrometer imports it to configure exemplar sampler
  creation.

## Core metrics

### `io.prometheus.metrics.core.metrics`

- `Counter`: D-core, D-migration, D-otel, J. It is a primary metric type in docs and is
  imported by JMX Exporter.
- `CounterWithCallback`: D-core. The callback docs list it as the counter callback metric
  type.
- `Gauge`: D-core, D-exporter, J. It is a primary metric type in docs and is imported by
  JMX Exporter.
- `GaugeWithCallback`: D-core. The callback docs list it as the gauge callback metric
  type and show examples.
- `Histogram`: D-core, D-config. It is a primary metric type in docs, including classic
  and native histogram configuration.
- `Info`: D-core, D-exporter, J. The metric type and OM2 docs describe info metrics, and
  JMX Exporter imports it for build info.
- `Metric`: S. Public common base for documented metric classes and their shared builder
  behavior.
- `MetricWithFixedMetadata`: S. Public base for documented metric classes whose metadata
  is fixed at build time.
- `SlidingWindow`: S. Public summary configuration type used by summary implementation
  and exposed through public summary behavior.
- `StateSet`: D-core. The metric type docs document StateSet as an OpenMetrics metric
  type and show builder usage.
- `StatefulMetric`: S. Public base for documented metric classes that expose label child
  handles.
- `Summary`: D-core, D-config, D-migration. It is a primary metric type in docs and is
  covered by documented quantile and age configuration.
- `SummaryWithCallback`: D-core. The callback docs list it as the summary callback metric
  type and show examples.

## Exporter common

### `io.prometheus.metrics.exporter.common`

- `PrometheusHttpExchange`: S. Public exchange abstraction used by the stable scrape
  handler so non-server adapters can plug in.
- `PrometheusHttpRequest`: S. Public request abstraction used by `PrometheusScrapeHandler`.
- `PrometheusHttpResponse`: S. Public response abstraction used by `PrometheusScrapeHandler`.
- `PrometheusScrapeHandler`: S, D-exporter. Shared public scrape handler behind documented
  HTTP and servlet exporters.

## HTTP server exporter

### `io.prometheus.metrics.exporter.httpserver`

- `DefaultHandler`: J, D-exporter. JMX Exporter imports it to configure HTTP endpoints;
  HTTP server docs mention default endpoint behavior.
- `HTTPServer`: D-core, D-exporter, D-otel, J. It is the documented standalone exporter
  and is imported by JMX Exporter.
- `HealthyHandler`: J, D-exporter. JMX Exporter imports it for the health endpoint; the
  HTTP server docs document health endpoint behavior.
- `MetricsHandler`: J, D-exporter. JMX Exporter imports it for the metrics endpoint; the
  HTTP server docs document metrics endpoint behavior.

## OpenTelemetry exporter

### `io.prometheus.metrics.exporter.opentelemetry`

- `OpenTelemetryExporter`: D-config, D-otel, J. The OTLP docs use its builder directly,
  and JMX Exporter imports it for OTel mode.

## PushGateway exporter

### `io.prometheus.metrics.exporter.pushgateway`

- `DefaultHttpConnectionFactory`: S. Public default implementation of the documented
  PushGateway connection extension point.
- `Format`: S, D-exporter. Public enum controlling the documented PushGateway wire format.
- `HttpConnectionFactory`: S. Public extension point accepted by `PushGateway.Builder`.
- `PushGateway`: D-config, D-exporter. The PushGateway docs use this type and its builder
  throughout.
- `Scheme`: S, D-config. Public scheme enum accepted by documented PushGateway builder
  and config paths.

## Servlet exporters

### `io.prometheus.metrics.exporter.servlet.jakarta`

- `PrometheusMetricsServlet`: D-exporter. Servlet and Spring docs show direct
  registration of the Jakarta servlet class.

### `io.prometheus.metrics.exporter.servlet.javax`

- `PrometheusMetricsServlet`: D-exporter. The javax artifact is the legacy servlet
  counterpart of the documented Jakarta servlet entry point.

## Exposition formats

### `io.prometheus.metrics.expositionformats`

- `ExpositionFormatWriter`: S. Public writer interface behind the documented exposition
  format selection and custom writer implementations.
- `ExpositionFormats`: M, D-exporter. Micrometer imports it for scraping, and exporter
  docs describe content negotiation between formats.
- `OpenMetrics2TextFormatWriter`: D-exporter. OM2 preview docs document the experimental
  OM2 writer path.
- `OpenMetricsTextFormatWriter`: M, D-exporter. Micrometer tests import it, and exporter
  docs describe OpenMetrics text output.
- `PrometheusProtobufWriter`: D-exporter. Exporter docs mention this class by name for
  shaded PushGateway jars.
- `PrometheusTextFormatWriter`: M, D-exporter. Micrometer tests import it, and exporter
  docs describe Prometheus text output.

## Instrumentation

### `io.prometheus.metrics.instrumentation.caffeine`

- `CacheMetricsCollector`: D-instr. The caffeine instrumentation docs describe registering
  this collector for cache metrics.

### `io.prometheus.metrics.instrumentation.dropwizard`

- `DropwizardExports`: S. Dropwizard 4 is a shipped instrumentation module and equivalent
  public entry point to the documented Dropwizard 5 exporter.

### `io.prometheus.metrics.instrumentation.dropwizard5`

- `DropwizardExports`: S. Dropwizard 5 is a shipped instrumentation module with public
  registration entry point.
- `InvalidMetricHandler`: S. Public extension point used by Dropwizard exports to decide
  how invalid Dropwizard metrics are handled.

### `io.prometheus.metrics.instrumentation.dropwizard5.labels`

- `CustomLabelMapper`: S. Public extension point for Dropwizard 5 custom label mapping.
- `MapperConfig`: S. Public configuration value consumed by `CustomLabelMapper`.

### `io.prometheus.metrics.instrumentation.guava`

- `CacheMetricsCollector`: D-instr. The guava instrumentation docs describe registering
  this collector for cache metrics.

### `io.prometheus.metrics.instrumentation.jvm`

- `JvmBufferPoolMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `JvmClassLoadingMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `JvmCompilationMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `JvmGarbageCollectorMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `JvmMemoryMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `JvmMemoryPoolAllocationMetrics`: S, D-instr. Public component metric used by aggregate
  JVM instrumentation.
- `JvmMetrics`: D-core, D-instr, D-migration, J. Quickstart, migration, and JMX Exporter
  use this aggregate JVM instrumentation entry point.
- `JvmNativeMemoryMetrics`: D-instr. Native memory tracking is documented as part of JVM
  instrumentation.
- `JvmRuntimeInfoMetric`: D-instr. The JVM docs link this metric class directly.
- `JvmThreadsMetrics`: S, D-instr. Public component metric used by aggregate JVM
  instrumentation.
- `ProcessMetrics`: S, D-instr. Public process metric component used by aggregate JVM
  instrumentation.

## Registry

### `io.prometheus.metrics.model.registry`

- `Collector`: D-core, S. Public extension point for custom collectors registered in a
  `PrometheusRegistry`.
- `MetricNameFilter`: S. Public filter accepted by registry scrape collection paths.
- `MetricType`: S. Public enum used in metadata and snapshot APIs.
- `MultiCollector`: M, J, D-core. Micrometer and JMX Exporter implement or import it, and
  multi-target docs show a custom collector pattern.
- `PrometheusRegistry`: D-core, D-migration, D-otel, M, J. It is the central registry in
  docs and a direct dependency of Micrometer and JMX Exporter.
- `PrometheusScrapeRequest`: D-core, S. Multi-target docs show scrape-request-aware
  collection; the type is required for that extension point.

## Snapshots

### `io.prometheus.metrics.model.snapshots`

- `ClassicHistogramBucket`: M, S. Micrometer uses classic histogram bucket snapshots, and
  this is the public value type for one bucket.
- `ClassicHistogramBuckets`: M, S. Micrometer uses it when reading histogram snapshots.
- `CounterSnapshot`: M, J, D-core. Used by Micrometer and JMX Exporter, and shown in
  multi-target custom collector examples.
- `DataPointSnapshot`: M, J, D-core. Common public base for snapshot data points used by
  Micrometer, JMX Exporter, and custom collector docs.
- `DistributionDataPointSnapshot`: S. Public base for histogram and summary data point
  snapshots returned by documented distribution metrics.
- `DuplicateLabelsException`: J, S. JMX Exporter tests catch duplicate-label behavior;
  custom collectors can encounter this public validation exception.
- `Exemplar`: M, D-otel. Micrometer creates exemplar snapshots, and OTel tracing docs
  describe exemplar output.
- `Exemplars`: M, D-otel. Micrometer creates exemplar containers, and OTel tracing docs
  describe exemplar output.
- `GaugeSnapshot`: M, J, D-core. Used by Micrometer and JMX Exporter, and shown in
  multi-target custom collector examples.
- `HistogramSnapshot`: M, D-core. Micrometer reads histogram snapshots, and histogram is a
  documented primary metric type.
- `InfoSnapshot`: M, J, D-core. Used by Micrometer and JMX Exporter for info metrics.
- `Label`: S. Public value type contained by `Labels` and snapshot APIs.
- `Labels`: M, J, D-core. Used by Micrometer and JMX Exporter, and shown in label and
  multi-target docs.
- `MetricMetadata`: M, J, S. Used by Micrometer and JMX Exporter when building custom
  snapshots.
- `MetricFamilyDescriptor`: S. Registration-time descriptor returned by the stable
  `Collector` and `MultiCollector` extension points.
- `MetricSnapshot`: M, J, D-core. Used by Micrometer and JMX Exporter, and shown in
  registry and multi-target docs.
- `MetricSnapshots`: M, J, D-core. Used by Micrometer and JMX Exporter, and returned by
  custom collector examples.
- `NativeHistogramBucket`: S. Public value type for native histogram snapshots, needed
  because native histograms are documented metric output.
- `NativeHistogramBuckets`: S. Public container for native histogram snapshot buckets.
- `PrometheusNaming`: M, J, D-core. Micrometer and JMX Exporter use it for name
  sanitizing, and multi-target docs show it directly.
- `Quantile`: S, D-core. Public value type for summary quantiles, which are documented in
  summary and callback APIs.
- `Quantiles`: M, D-core. Micrometer uses it and summary docs expose quantile behavior.
- `StateSetSnapshot`: S, D-core. Public snapshot type for the documented StateSet metric.
- `SummarySnapshot`: M, D-core. Micrometer reads summary snapshots, and summary is a
  documented primary metric type.
- `Unit`: J, S. JMX Exporter imports it, and unit metadata appears throughout documented
  metric and naming behavior.
- `UnknownSnapshot`: J, S. JMX Exporter builds unknown snapshots for untyped JMX metrics.

## Simpleclient bridge

### `io.prometheus.metrics.simpleclient.bridge`

- `SimpleclientCollector`: D-migration. Migration docs use this bridge as the supported
  path for simpleclient 0.x metrics.

## Tracer common

### `io.prometheus.metrics.tracer.common`

- `SpanContext`: M, D-otel. Micrometer imports it for exemplars, and OTel tracing docs
  describe span context propagation into exemplars.

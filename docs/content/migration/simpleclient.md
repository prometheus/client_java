---
title: Simpleclient
weight: 1
---

The Prometheus Java client library 1.0.0 is a complete rewrite, and is not backwards compatible with the old `simpleclient` modules for a variety of reasons:

* We rewrote the underlying data model. The `simpleclient` data model is based on [OpenMetrics](https://openmetrics.io/), which is based on the assumption that each data point has exactly one `double` value (`Collector.Sample.value` in `simpleclient`). With the new Prometheus native histograms this is no longer true. Native histograms have a complex data structure as value. This is the reason why native histograms cannot be exposed in OpenMetrics text format. The new `prometheus-metrics-model` implements the current Prometheus data model and is not based on OpenMetrics.
* We refactored the package names. The simpleclient has package names that are reused by multiple Maven modules, which causes issues with the Java module system. The new `prometheus-metrics` modules each has their own package name, package names are not reused.

Migrating from Simpleclient
---------------------------

For users of previous versions of the Prometheus Java client we provide a migration module for bridging the simpleclient `CollectorRegistry` to the new `PromethesuRegistry`.

To use the bridge, add the following dependency:

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-simpleclient-bridge:1.0.0'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-simpleclient-bridge</artifactId>
    <version>1.0.0</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

Then add the following to your code:

```java
SimpleclientCollector.builder().register();
```

This will make all metrics registered with simpleclient's `CollectorRegistry.defaultRegistry` available in the new `PrometheusRegistry.defaultRegistry`.

If you are using custom registries, you can specify them like this:

```java
CollectorRegistry simpleclientRegistry = ...;
PrometheusRegistry prometheusRegistry = ...;

SimpleclientCollector.builder()
    .collectorRegistry(simpleclientRegistry)
    .register(prometheusRegistry);
```

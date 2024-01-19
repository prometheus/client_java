---
title: Simpleclient
weight: 1
---

The Prometheus Java client library 1.0.0 is a complete rewrite of the underlying data model, and is not backwards compatible with releases 0.16.0 and older for a variety of reasons:

* The old data model was based on [OpenMetrics](https://openmetrics.io). Native histograms don't fit with the OpenMetrics model because they don't follow the "every sample has exactly one double value" paradigm. It was a lot cleaner to implement a dedicated `prometheus-metrics-model` than trying to fit native histograms into the existing OpenMetrics-based model.
* Version 0.16.0 and older has multiple Maven modules sharing the same Java package name. This is not supported by the Java module system. To support users of Java modules, we renamed all packages and made sure no package is reused across multiple Maven modules.

Migration using the Simpleclient Bridge
---------------------------------------

Good news: Users of version 0.16.0 and older do not need to refactor all their instrumentation code to get started with 1.0.0.

We provide a migration module for bridging the old simpleclient `CollectorRegistry` to the new `PromethesuRegistry`.

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

Refactoring the Instrumentation Code
------------------------------------

If you decide to get rid of the old 0.16.0 dependencies and use 1.0.0 only, you need to refactor your code:

Dependencies:

* `simpleclient` -> `prometheus-metrics-core`
* `simpleclient_hotspot` -> `prometheus-metrics-instrumentation-jvm`
* `simpleclient_httpserver` -> `prometheus-metrics-exporter-httpserver`
* `simpleclient_servlet_jakarta` -> `prometheus-metrics-exporter-servlet-jakarta`

As long as you are using high-level metric API like `Counter`, `Gauge`, `Histogram`, and `Summary` converting code to the new API is relatively straightforward. You will need to adapt the package name and apply some minor changes like using `builder()` instead of `build()` or using `labelValues()` instead of `labels()`.

Example of the old 0.16.0 API:

```java
import io.prometheus.client.Counter;

Counter counter = Counter.build()
        .name("test")
        .help("test counter")
        .labelNames("path")
        .register();

counter.labels("/hello-world").inc();
```

Example of the new 1.0.0 API:

```java
import io.prometheus.metrics.core.metrics.Counter;

Counter counter = Counter.builder()
        .name("test")
        .help("test counter")
        .labelNames("path")
        .register();

counter.labelValues("/hello-world").inc();
```

Reasons why we changed the API: Changing the package names was a necessity because the previous package names were incompatible with the Java module system. However, renaming packages requires changing code anyway, so we decided to clean up some things. For example, the name `builder()` for a builder method is very common in the Java ecosystem, it's used in Spring, Lombok, and so on. So naming the method `builder()` makes the Prometheus library more aligned with the broader Java ecosystem.

If you are using the low level `Collector` API directly, you should have a look at the new callback metric types, see [/getting-started/callbacks/](../../getting-started/callbacks/). Chances are good that the new callback metrics have an easier way to achieve what you need than the old 0.16.0 code.

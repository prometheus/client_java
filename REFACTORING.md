# Why Refactoring

The current data model produced by `Collector.collect()` is based on the OpenMetrics text format.
It assumes that every metric has a list of Samples, and each Sample has exactly one double value
(see `MetricFamilySamples.Sample.value`).

The new exponential histograms don't fit into that model, and they don't have a representation in text format.

# Goals

As we need to change the `Collector.collect()` return type anyway, we will use this opportunity for a larger
non-backwards compatible refactoring.

* Decoupling the `Collector` interface form the OpenMetrics Text format exposition format. Currently, there is
  a separate maven module `simpleclient_common` that implements the text format. However, the text format is
  also tightly coupled to the `Collector` type in the `simpleclient` module itself, as the `collect()` method
  produces Java classes that map 1:1 to text format. In the future, we want to support multiple exposition formats
  (text format, Prometheus protobuf format, OpenTelemetry protobuf format). The return type of `collect()` should
  not be coupled to any specific exposition format.
* Better thread safety support: The `collect()` method returns a consistent snapshot of the current metric state.
  This should be a non-blocking operation. Serializing the snapshot to an exposition format should not block
  other threads from updating the metric.
* Restructuring packages: Java modules, OSGI
* Renaming Maven modules
* Decouple logic (timer, etc.) from core type (single responsibility, was not so important when we had just counters, but with summaries and sparse histograms more important)

# New Model

`Collector.collect()` returns Snapshot. There are exactly 6 implementations:

Unmodifyable Key including name and labels.

Drop Java 6 support.

* CounterSnapshot
* 

# Naming

OpenMetrics is not an API. It's an exposition format.

Trying to use OpenMetrics terminology 1-to-1 as API for a metrics library will lead to confusion.

Example: The term _MetricFamily_. Look at `Counter` in `client_java` 0.16.1:

```java
Counter counter1 = Counter.build("requests_total", "help")
        .register();

Counter counter2 = Counter.build("requests_total", "help")
        .labelNames("http_status")
        .register();
```

In OpenMetrics terminology `counter1` would be a _MetricFamily_, and `counter2` would be a _Metric_.
However, as an API this distinction doesn't work, both are of type `Counter`.

With the refactoring, we'll use the term _Metric_. The goal is to use a name that sounds natural in plain
English (the term "counter metric" sounds natural to me), and not use a name that's closely aligned with
the specification of the OpenMetrics exposition format.

Fun fact: An actual metric in `counter2` is of type `Child`, which is a `client_java`-specific term that's neither
part of OpenMetrics nor of the Prometheus model.

# todo

OTel:
* allow dots in label names and attribute names?
* how to deal with resource attributes / info metrics?

# opinionated

From writing clientlibs:

> Client libraries MUST NOT allow users to have different label names for the same metric for Gauge/Counter/Summary/Histogram or any other Collector offered by the library.

This is not like in Micrometer, where you can create labels on the fly.

Note: You can still create metrics iwth different label names and the same metric name, but you'll have to write your own implementation, not use Counter/Gage/...

# market

Micrometer and OpenTelemetry are generic multi-vendor, client_java is opinionated Prometheus.
Micrometer and OpenTelemetry SDKs include tracing, client_java is metrics only (with Exemplar support for 3rd party tracers).
client_java will be a simpler, lightweight alternative.

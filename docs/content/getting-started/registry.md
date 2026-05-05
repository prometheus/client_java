---
title: Registry
weight: 2
---

In order to expose metrics, you need to register them with a `PrometheusRegistry`. We are using a
counter as an example here, but the `register()` method is the same for all metric types.

## Registering a Metric with the Default Registry

```java
Counter eventsTotal = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(); // <-- implicitly uses PrometheusRegistry.defaultRegistry
```

The `register()` call above builds the counter and registers it with the global static
`PrometheusRegistry.defaultRegistry`. Using the default registry is recommended.

## Registering a Metric with a Custom Registry

You can also register your metric with a custom registry:

```java
PrometheusRegistry myRegistry = new PrometheusRegistry();

Counter eventsTotal = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(myRegistry);
```

## Registering a Metric with Multiple Registries

As an alternative to calling `register()` directly, you can `build()` metrics without registering
them,
and register them later:

```java

// create a counter that is not registered with any registry

Counter eventsTotal = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .build(); // <-- this will create the metric but not register it

// register the counter with the default registry

PrometheusRegistry.defaultRegistry.register(eventsTotal);

// register the counter with a custom registry.
// This is OK, you can register a metric with multiple registries.

PrometheusRegistry myRegistry = new PrometheusRegistry();
myRegistry.register(eventsTotal);
```

Custom registries are useful if you want to maintain different scopes of metrics, like
a debug registry with a lot of metrics, and a default registry with only a few metrics.

## IllegalArgumentException: Duplicate Metric Name in Registry

While it is OK to register the same metric with multiple registries, it is illegal to register the
same metric name multiple times with the same registry.
The following code will throw an `IllegalArgumentException`:

```java
Counter eventsTotal1 = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register();

Counter eventsTotal2 = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(); // IllegalArgumentException, because a metric with that name is already registered
```

## Suffix-Based Name Validation

Suffix handling happens at scrape time. This makes metric names more flexible while keeping the
exposed output unambiguous.

The registry now tracks not only the metric names you register, but also the exposition names they
would claim in OpenMetrics 1.x and Prometheus text format, such as `_total`, `_count`, `_sum`,
`_bucket`, `_created`, and `_info`.

This means names are accepted when they are safe, and combinations are rejected when they would
collide at scrape time. The table below also shows the pre-1.6.0 behavior for comparison.

| Example                                       | Before 1.6.0 | Current behavior | Why                                                                                               |
| --------------------------------------------- | ------------ | ---------------- | ------------------------------------------------------------------------------------------------- |
| `Gauge("foo_total")`                          | Rejected     | Allowed          | Safe because `_total` suffix expansion applies to counters, not gauges.                           |
| `Counter("events_total")`                     | Rejected     | Allowed          | Safe because the OM1 output is `events_total`; the writer avoids double-appending `_total`.       |
| `Gauge("foo_total")` + `Histogram("foo")`     | Rejected     | Allowed          | Safe because the exposed names do not overlap: `foo_total` vs `foo_bucket`/`foo_count`/`foo_sum`. |
| `Gauge("events_total")` + `Counter("events")` | Rejected     | Rejected         | Rejected because both would expose `events_total` in OM1.                                         |
| `Gauge("foo_count")` + `Histogram("foo")`     | Allowed      | Rejected         | Rejected because both would claim `foo_count` at scrape time.                                     |

## Validation at registration only

Validation of duplicate metric names and label schemas happens at registration time only.
Built-in metrics (Counter, Gauge, Histogram, etc.) participate in this validation.

Custom collectors that implement the `Collector` or `MultiCollector` interface can optionally
implement `getPrometheusName()` and `getMetricType()` (and the MultiCollector per-name variants) so
the registry can enforce consistency. **Validation is skipped when metric name or type is
unavailable:** if `getPrometheusName()` or `getMetricType()` returns `null`, the registry does not
validate that collector. If two such collectors produce the same metric name and same label set at
scrape time, the exposition output may contain duplicate time series and be invalid for Prometheus.

When validation _is_ performed (name and type are non-null), **null label names are treated as an
empty label schema:** `getLabelNames()` returning `null` is normalized to `Collections.emptySet()`
and full label-schema validation and duplicate detection still apply. A collector that returns a
non-null type but leaves `getLabelNames()` as `null` is still validated, with its labels treated as
empty.

This is also relevant for downstream adapter libraries that bridge to this registry. If an adapter
implements `MultiCollector`, its registration-time metadata must match the metric families it will
actually emit at scrape time. In practice, that means `getPrometheusNames()`, `getMetricType(...)`,
`getLabelNames(...)`, and `getMetadata(...)` need to describe the same names, types, labels, and
suffix behavior as the eventual `MetricSnapshot` output. Otherwise an adapter may pass or fail
collision checks differently after upgrading to a newer client_java release, even if its scrape
output logic did not change.

## Unregistering a Metric

There is no automatic expiry of unused metrics (yet), once a metric is registered it will remain
registered forever.

However, you can programmatically unregister an obsolete metric like this:

```java
PrometheusRegistry.defaultRegistry.unregister(eventsTotal);
```

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

## Filtering Metrics

You can set a registry-level metric name filter that applies to all scrape operations.
Only metrics whose names match the filter predicate will be included in scrape results:

```java
PrometheusRegistry.defaultRegistry.setMetricFilter(name -> !name.startsWith("debug_"));
```

The registry filter is AND-combined with any scrape-time `includedNames` predicate passed to
`scrape(Predicate)`. For example, if the registry filter allows `counter_*` and the scrape-time
filter allows `counter_a`, only `counter_a` will be included.

To remove the filter, set it to `null`:

```java
PrometheusRegistry.defaultRegistry.setMetricFilter(null);
```

Note that `clear()` does not reset the metric filter -- it only removes registered collectors.

## Unregistering a Metric

There is no automatic expiry of unused metrics (yet), once a metric is registered it will remain
registered forever.

However, you can programmatically unregister an obsolete metric like this:

```java
PrometheusRegistry.defaultRegistry.unregister(eventsTotal);
```

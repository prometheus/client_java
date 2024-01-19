---
title: Registry
weight: 2
---

In order to expose metrics, you need to register them with a `PrometheusRegistry`. We are using a counter as an example here, but the `register()` method is the same for all metric types.

Registering a Metrics with the Default Registry
-----------------------------------------------

```java
Counter eventsTotal = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(); // <-- implicitly uses PrometheusRegistry.defaultRegistry
```

The `register()` call above builds the counter and registers it with the global static `PrometheusRegistry.defaultRegistry`. Using the default registry is recommended.

Registering a Metrics with a Custom Registry
--------------------------------------------

You can also register your metric with a custom registry:

```java
PrometheusRegistry myRegistry = new PrometheusRegistry();

Counter eventsTotal = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(myRegistry);
```

Registering a Metric with Multiple Registries
---------------------------------------------

As an alternative to calling `register()` directly, you can `build()` metrics without registering them,
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
// This is ok, you can register a metric with multiple registries.

PrometheusRegistry myRegistry = new PrometheusRegistry();
myRegistry.register(eventsTotal);
```

Custom registries are useful if you want to maintain different scopes of metrics, like
a debug registry with a lot of metrics, and a default registry with only a few metrics.

IllegalArgumentException: Duplicate Metric Name in Registry
-----------------------------------------------------------

While it is ok to register the same metric with multiple registries, it is illegal to register the same metric name multiple times with the same registry.
The following code will throw an `IllegalArgumentException`:

```java
Counter eventsTotal1 = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register();

Counter eventsTotal2 = Counter.builder()
    .name("events_total")
    .help("Total number of events")
    .register(); // <-- IllegalArgumentException, because a metric with that name is already registered
```

Unregistering a Metric
----------------------

There is no automatic expiry of unused metrics (yet), once a metric is registered it will remain registered forever.

However, you can programmatically unregistered an obsolete metric like this:

```java
PrometheusRegistry.defaultRegistry.unregister(eventsTotal);
```

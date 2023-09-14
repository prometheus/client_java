---
title: Callbacks
weight: 5
---

The section on [metric types](../metric-types) showed how to use metrics that actively maintain their state.

This section shows how to create callback-based metrics, i.e. metrics that invoke a callback at scrape time to get the current values.

For example, let's assume we have two instances of a `Cache`, a `coldCache` and a `hotCache`. The following implements a callback-based `cache_size_bytes` metric:

```java
Cache coldCache = new Cache();
Cache hotCache = new Cache();

GaugeWithCallback.builder()
    .name("cache_size_bytes")
    .help("Size of the cache in Bytes.")
    .unit(Unit.BYTES)
    .labelNames("state")
    .callback(callback -> {
        callback.call(coldCache.sizeInBytes(), "cold");
        callback.call(hotCache.sizeInBytes(), "hot");
    })
    .register();
```

The resulting text format looks like this:

```
# TYPE cache_size_bytes gauge
# UNIT cache_size_bytes bytes
# HELP cache_size_bytes Size of the cache in Bytes.
cache_size_bytes{state="cold"} 78.0
cache_size_bytes{state="hot"} 83.0
```

Better examples of callback metrics can be found in the `prometheus-metrics-instrumentation-jvm` module.

The available callback metric types are:

* `GaugeWithCallback` for gauges.
* `CounterWithCallback` for counters.
* `SummaryWithCallback` for summaries.

The API for gauges and counters is very similar. For summaries the callback has a few more parameters, because it accepts a count, a sum, and quantiles:

```java
SummaryWithCallback.builder()
    .name("example_callback_summary")
    .help("help message.")
    .labelNames("status")
    .callback(callback -> {
        callback.call(cache.getCount(), cache.getSum(), Quantiles.EMPTY, "ok");
    })
    .register();
```

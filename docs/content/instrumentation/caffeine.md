---
title: Caffeine Cache
weight: 1
---

The Caffeine instrumentation module, added in version 1.3.2, translates observability data
provided by caffeine `Cache` objects into prometheus metrics.

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-instrumentation-caffeine:1.3.2'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-instrumentation-caffeine</artifactId>
    <version>1.3.2</version>
</dependency>
```
{{< /tab >}}
{{< /tabs >}}

In order to collect metrics:

 * A single `CacheMetricsCollector` instance must be registered with the registry;
   * Multiple `CacheMetricsCollector` instances cannot be registered with the same registry;
 * The `Cache` object must be instantiated with the `recordStats()` option, and then added to the
   `CacheMetricsCollector` instance with a unique name, which will be used as the value of the
   `cache` label on the exported metrics;
   * If the `recordStats` option is not set, most metrics will only return zero values;

```java
var cache = Caffeine.newBuilder().recordStats().build();
var cacheMetrics = CacheMetricsCollector.builder().build();
PrometheusRegistry.defaultRegistry.register(cacheMetrics);
cacheMetrics.addCache("mycache", cache);
```

{{< hint type=note >}}

In version 1.3.5 and older of the caffeine instrumentation library, `CacheMetricsCollector.builder`
does not exist, i.e. a constructor call `new CacheMetricsCollector()` is the only option.

{{< /hint >}}

All example metrics on this page will use the `mycache` label value.

Generic Cache Metrics
---------------------

For all cache instances, the following metrics will be available:

```
# TYPE caffeine_cache_hit counter
# HELP caffeine_cache_hit Cache hit totals
caffeine_cache_hit_total{cache="mycache"} 10.0
# TYPE caffeine_cache_miss counter
# HELP caffeine_cache_miss Cache miss totals
caffeine_cache_miss_total{cache="mycache"} 3.0
# TYPE caffeine_cache_requests counter
# HELP caffeine_cache_requests Cache request totals, hits + misses
caffeine_cache_requests_total{cache="mycache"} 13.0
# TYPE caffeine_cache_eviction counter
# HELP caffeine_cache_eviction Cache eviction totals, doesn't include manually removed entries
caffeine_cache_eviction_total{cache="mycache"} 1.0
# TYPE caffeine_cache_estimated_size
# HELP caffeine_cache_estimated_size Estimated cache size
caffeine_cache_estimated_size{cache="mycache"} 5.0
```

Loading Cache Metrics
---------------------

If the cache is an instance of `LoadingCache`, i.e. it is built with a `loader` function that is
managed by the cache library, then metrics for observing load time and load failures become
available:

```
# TYPE caffeine_cache_load_failure counter
# HELP caffeine_cache_load_failure Cache load failures
caffeine_cache_load_failure_total{cache="mycache"} 10.0
# TYPE caffeine_cache_loads counter
# HELP caffeine_cache_loads Cache loads: both success and failures
caffeine_cache_loads_total{cache="mycache"} 3.0
# TYPE caffeine_cache_load_duration_seconds summary
# HELP caffeine_cache_load_duration_seconds Cache load duration: both success and failures
caffeine_cache_load_duration_seconds_count{cache="mycache"} 7.0
caffeine_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
```

Weighted Cache Metrics
----------------------

Two metrics exist for observability specifically of caches that define a `weigher`:

```
# TYPE caffeine_cache_eviction_weight counter
# HELP caffeine_cache_eviction_weight Weight of evicted cache entries, doesn't include manually removed entries
caffeine_cache_eviction_weight_total{cache="mycache"} 5.0
# TYPE caffeine_cache_weighted_size gauge
# HELP caffeine_cache_weighted_size Approximate accumulated weight of cache entries
caffeine_cache_weighted_size{cache="mycache"} 30.0
```

{{< hint type=note >}}

`caffeine_cache_weighted_size` is available only if the cache instance defines a `maximumWeight`.

{{< /hint >}}

Up to version 1.3.5 and older, the weighted metrics had a different behavior:

 * `caffeine_cache_weighted_size` was not implemented;
 * `caffeine_cache_eviction_weight` was exposed as a `gauge`;

It is possible to restore the behavior of version 1.3.5 and older, by either:

 * Using the deprecated `new CacheMetricsCollector()` constructor;
 * Using the flags provided on the `CacheMetricsCollector.Builder` object to opt-out of each of the
   elements of the post-1.3.5 behavior:
   * `builder.collectWeightedSize(false)` will disable collection of `caffeine_cache_weighted_size`;
   * `builder.collectEvictionWeightAsCounter(false)` will expose `caffeine_cache_eviction_weight` as
     a `gauge` metric;

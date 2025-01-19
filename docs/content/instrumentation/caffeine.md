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
var cacheMetrics = new CacheMetricsCollector();
PrometheusRegistry.defaultRegistry.register(cacheMetrics);
cacheMetrics.addCache("mycache", cache);
```

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

If the cache is weighted, i.e. it defines a `weigher` function, then the following metrics
become interesting:

```
# TYPE caffeine_cache_eviction_weight gauge
# HELP caffeine_cache_eviction_weight Cache eviction weight
caffeine_cache_eviction_weight{cache="mycache"} 5.0
```

Note: while `caffeine_cache_eviction_weight` is exported as a `gauge` metric, it represents
a monotonicaly increasing value. Also, in the case where the cache does not define a `weigher`
function, it will return the same values as `caffeine_cache_eviction_total`.
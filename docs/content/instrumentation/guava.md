---
title: Guava Cache
weight: 1
---

The Guava instrumentation module, added in version 1.3.2, translates observability data
provided by Guava `Cache` objects into prometheus metrics.

{{< tabs "uniqueid" >}}
{{< tab "Gradle" >}}
```
implementation 'io.prometheus:prometheus-metrics-instrumentation-guava:1.3.2'
```
{{< /tab >}}
{{< tab "Maven" >}}
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>prometheus-metrics-instrumentation-guava</artifactId>
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
var cache = CacheBuilder.newBuilder().recordStats().build();
var cacheMetrics = new CacheMetricsCollector();
PrometheusRegistry.defaultRegistry.register(cacheMetrics);
cacheMetrics.addCache("mycache", cache);
```

All example metrics on this page will use the `mycache` label value.

Generic Cache Metrics
---------------------

For all cache instances, the following metrics will be available:

```
# TYPE guava_cache_hit counter
# HELP guava_cache_hit Cache hit totals
guava_cache_hit_total{cache="mycache"} 10.0
# TYPE guava_cache_miss counter
# HELP guava_cache_miss Cache miss totals
guava_cache_miss_total{cache="mycache"} 3.0
# TYPE guava_cache_requests counter
# HELP guava_cache_requests Cache request totals
guava_cache_requests_total{cache="mycache"} 13.0
# TYPE guava_cache_eviction counter
# HELP guava_cache_eviction Cache eviction totals, doesn't include manually removed entries
guava_cache_eviction_total{cache="mycache"} 1.0
# TYPE guava_cache_size
# HELP guava_cache_size Cache size
guava_cache_size{cache="mycache"} 5.0
```

Loading Cache Metrics
---------------------

If the cache is an instance of `LoadingCache`, i.e. it is built with a `loader` function that is
managed by the cache library, then metrics for observing load time and load failures become
available:

```
# TYPE guava_cache_load_failure counter
# HELP guava_cache_load_failure Cache load failures
guava_cache_load_failure_total{cache="mycache"} 10.0
# TYPE guava_cache_loads counter
# HELP guava_cache_loads Cache loads: both success and failures
guava_cache_loads_total{cache="mycache"} 3.0
# TYPE guava_cache_load_duration_seconds summary
# HELP guava_cache_load_duration_seconds Cache load duration: both success and failures
guava_cache_load_duration_seconds_count{cache="mycache"} 7.0
guava_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
```

package io.prometheus.client.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Collect metrics from Guava's com.google.common.cache.Cache.
 * <p>
 * <pre>{@code
 *
 * // Note that `recordStats()` is required to gather non-zero statistics
 * Cache<String, String> cache = CacheBuilder.newBuilder().recordStats().build();
 * CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();
 * cacheMetrics.addCache("mycache", cache);
 *
 * }</pre>
 *
 * Exposed metrics are labeled with the provided cache name.
 *
 * With the example above, sample metric names would be:
 * <pre>
 *     guava_cache_hit_total{cache="mycache"} 10.0
 *     guava_cache_miss_total{cache="mycache"} 3.0
 *     guava_cache_requests_total{cache="mycache"} 13.0
 *     guava_cache_eviction_total{cache="mycache"} 1.0
 *     guava_cache_size{cache="mycache"} 5.0
 * </pre>
 *
 * Additionally if the cache includes a loader, the following metrics would be provided:
 * <pre>
 *     guava_cache_load_failure_total{cache="mycache"} 2.0
 *     guava_cache_loads_total{cache="mycache"} 7.0
 *     guava_cache_load_duration_seconds_count{cache="mycache"} 7.0
 *     guava_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
 * </pre>
 *
 */
public class CacheMetricsCollector extends Collector {

    protected final ConcurrentMap<String, Cache> children = new ConcurrentHashMap<String, Cache>();

    /**
     * Add or replace the cache with the given name.
     * <p>
     * Any references any previous cache with this name is invalidated.
     *
     * @param cacheName The name of the cache, will be the metrics label value
     * @param cache The cache being monitored
     */
    public void addCache(String cacheName, Cache cache) {
        children.put(cacheName, cache);
    }

    /**
     * Remove the cache with the given name.
     * <p>
     * Any references to the cache are invalidated.
     *
     * @param cacheName cache to be removed
     */
    public Cache removeCache(String cacheName) {
        return children.remove(cacheName);
    }

    /**
     * Remove all caches.
     * <p>
     * Any references to all caches are invalidated.
     */
    public void clear(){
        children.clear();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        List<String> labelNames = Arrays.asList("cache");

        CounterMetricFamily cacheHitTotal = new CounterMetricFamily("guava_cache_hit_total",
                "Cache hit totals", labelNames);
        mfs.add(cacheHitTotal);

        CounterMetricFamily cacheMissTotal = new CounterMetricFamily("guava_cache_miss_total",
                "Cache miss totals", labelNames);
        mfs.add(cacheMissTotal);

        CounterMetricFamily cacheRequestsTotal = new CounterMetricFamily("guava_cache_requests_total",
                "Cache request totals, hits + misses", labelNames);
        mfs.add(cacheRequestsTotal);

        CounterMetricFamily cacheEvictionTotal = new CounterMetricFamily("guava_cache_eviction_total",
                "Cache eviction totals, doesn't include manually removed entries", labelNames);
        mfs.add(cacheEvictionTotal);

        CounterMetricFamily cacheLoadFailure = new CounterMetricFamily("guava_cache_load_failure_total",
                "Cache load failures", labelNames);
        mfs.add(cacheLoadFailure);

        CounterMetricFamily cacheLoadTotal = new CounterMetricFamily("guava_cache_loads_total",
                "Cache loads: both success and failures", labelNames);
        mfs.add(cacheLoadTotal);

        GaugeMetricFamily cacheSize = new GaugeMetricFamily("guava_cache_size",
                "Cache size", labelNames);
        mfs.add(cacheSize);

        SummaryMetricFamily cacheLoadSummary = new SummaryMetricFamily("guava_cache_load_duration_seconds",
                "Cache load duration: both success and failures", labelNames);
        mfs.add(cacheLoadSummary);

        for(Map.Entry<String, Cache> c: children.entrySet()) {
            List<String> cacheName = Arrays.asList(c.getKey());
            CacheStats stats = c.getValue().stats();

            cacheHitTotal.addMetric(cacheName, stats.hitCount());
            cacheMissTotal.addMetric(cacheName, stats.missCount());
            cacheRequestsTotal.addMetric(cacheName, stats.requestCount());
            cacheEvictionTotal.addMetric(cacheName, stats.evictionCount());
            cacheSize.addMetric(cacheName, c.getValue().size());

            if(c.getValue() instanceof LoadingCache) {
                cacheLoadFailure.addMetric(cacheName, stats.loadExceptionCount());
                cacheLoadTotal.addMetric(cacheName, stats.loadCount());

                cacheLoadSummary.addMetric(cacheName, stats.loadCount(), stats.totalLoadTime() / Collector.NANOSECONDS_PER_SECOND);
            }
        }
        return mfs;
    }
}

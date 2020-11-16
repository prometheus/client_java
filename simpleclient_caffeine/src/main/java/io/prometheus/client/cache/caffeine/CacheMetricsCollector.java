package io.prometheus.client.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Collect metrics from Caffeine's com.github.benmanes.caffeine.cache.Cache.
 * <p>
 * <pre>{@code
 *
 * // Note that `recordStats()` is required to gather non-zero statistics
 * Cache<String, String> cache = Caffeine.newBuilder().recordStats().build();
 * CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();
 * cacheMetrics.addCache("mycache", cache);
 *
 * }</pre>
 *
 * Exposed metrics are labeled with the provided cache name.
 *
 * With the example above, sample metric names would be:
 * <pre>
 *     caffeine_cache_hit_total{cache="mycache"} 10.0
 *     caffeine_cache_miss_total{cache="mycache"} 3.0
 *     caffeine_cache_requests_total{cache="mycache"} 13.0
 *     caffeine_cache_eviction_total{cache="mycache"} 1.0
 *     caffeine_cache_estimated_size{cache="mycache"} 5.0
 * </pre>
 *
 * Additionally if the cache includes a loader, the following metrics would be provided:
 * <pre>
 *     caffeine_cache_load_failure_total{cache="mycache"} 2.0
 *     caffeine_cache_loads_total{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_count{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
 * </pre>
 *
 */
public class CacheMetricsCollector extends Collector {
    protected final ConcurrentMap<String, Cache> children = new ConcurrentHashMap<String, Cache>();
    
    private final Map<String, String> customLabels;
    
    /**
     * Initialize collector without custom labels.
     */
    public CacheMetricsCollector() {
        this(Collections.<String, String>emptyMap());
    }
    
    /**
     * Initialize collector with custom labels.
     *
     * @param customLabels labels to add to each metric being recorded.
     */
    public CacheMetricsCollector(Map<String, String> customLabels) {
        this.customLabels = Collections.unmodifiableMap(customLabels);
    }
    
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
     * Add or replace the cache with the given name.
     * <p>
     * Any references any previous cache with this name is invalidated.
     *
     * @param cacheName The name of the cache, will be the metrics label value
     * @param cache The cache being monitored
     */
    public void addCache(String cacheName, AsyncCache cache) {
        children.put(cacheName, cache.synchronous());
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
        List<String> labelNames = new ArrayList<String>(customLabels.size() + 1) {{
            add("cache");
            for (Map.Entry<String, String> entry : customLabels.entrySet()) {
                add(entry.getKey());
            }
        }};

        CounterMetricFamily cacheHitTotal = new CounterMetricFamily("caffeine_cache_hit_total",
                "Cache hit totals", labelNames);
        mfs.add(cacheHitTotal);

        CounterMetricFamily cacheMissTotal = new CounterMetricFamily("caffeine_cache_miss_total",
                "Cache miss totals", labelNames);
        mfs.add(cacheMissTotal);

        CounterMetricFamily cacheRequestsTotal = new CounterMetricFamily("caffeine_cache_requests_total",
                "Cache request totals, hits + misses", labelNames);
        mfs.add(cacheRequestsTotal);

        CounterMetricFamily cacheEvictionTotal = new CounterMetricFamily("caffeine_cache_eviction_total",
                "Cache eviction totals, doesn't include manually removed entries", labelNames);
        mfs.add(cacheEvictionTotal);

        GaugeMetricFamily cacheEvictionWeight = new GaugeMetricFamily("caffeine_cache_eviction_weight",
                "Cache eviction weight", labelNames);
        mfs.add(cacheEvictionWeight);

        CounterMetricFamily cacheLoadFailure = new CounterMetricFamily("caffeine_cache_load_failure_total",
                "Cache load failures", labelNames);
        mfs.add(cacheLoadFailure);

        CounterMetricFamily cacheLoadTotal = new CounterMetricFamily("caffeine_cache_loads_total",
                "Cache loads: both success and failures", labelNames);
        mfs.add(cacheLoadTotal);

        GaugeMetricFamily cacheSize = new GaugeMetricFamily("caffeine_cache_estimated_size",
                "Estimated cache size", labelNames);
        mfs.add(cacheSize);

        SummaryMetricFamily cacheLoadSummary = new SummaryMetricFamily("caffeine_cache_load_duration_seconds",
                "Cache load duration: both success and failures", labelNames);
        mfs.add(cacheLoadSummary);

        for(final Map.Entry<String, Cache> c: children.entrySet()) {
            
            List<String> labelValues = new ArrayList<String>(customLabels.size() + 1) {{
                add(c.getKey());
                for (Map.Entry<String, String> entry : customLabels.entrySet()) {
                    add(entry.getValue());
                }
            }};
            
            CacheStats stats = c.getValue().stats();

            try{
                cacheEvictionWeight.addMetric(labelValues, stats.evictionWeight());
            } catch (Exception e) {
                // EvictionWeight metric is unavailable, newer version of Caffeine is needed.
            }

            cacheHitTotal.addMetric(labelValues, stats.hitCount());
            cacheMissTotal.addMetric(labelValues, stats.missCount());
            cacheRequestsTotal.addMetric(labelValues, stats.requestCount());
            cacheEvictionTotal.addMetric(labelValues, stats.evictionCount());
            cacheSize.addMetric(labelValues, c.getValue().estimatedSize());

            if(c.getValue() instanceof LoadingCache) {
                cacheLoadFailure.addMetric(labelValues, stats.loadFailureCount());
                cacheLoadTotal.addMetric(labelValues, stats.loadCount());

                cacheLoadSummary.addMetric(labelValues, stats.loadCount(), stats.totalLoadTime() / Collector.NANOSECONDS_PER_SECOND);
            }
        }
        return mfs;
    }
}

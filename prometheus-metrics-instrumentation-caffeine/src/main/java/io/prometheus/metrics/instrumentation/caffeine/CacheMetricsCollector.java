package io.prometheus.metrics.instrumentation.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;

import java.util.Arrays;
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
 * Additionally, if the cache includes a loader, the following metrics would be provided:
 * <pre>
 *     caffeine_cache_load_failure_total{cache="mycache"} 2.0
 *     caffeine_cache_loads_total{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_count{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
 * </pre>
 *
 */
public class CacheMetricsCollector implements MultiCollector {
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;

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
    public MetricSnapshots collect() {
        final MetricSnapshots.Builder metricSnapshotsBuilder = MetricSnapshots.builder();
        final List<String> labelNames = Arrays.asList("cache");

        final CounterSnapshot.Builder cacheHitTotal = CounterSnapshot.builder()
            .name("caffeine_cache_hit")
            .help("Cache hit totals");

        final CounterSnapshot.Builder cacheMissTotal = CounterSnapshot.builder()
            .name("caffeine_cache_miss")
            .help("Cache miss totals");

        final CounterSnapshot.Builder cacheRequestsTotal = CounterSnapshot.builder()
            .name("caffeine_cache_requests")
            .help("Cache request totals, hits + misses");

        final CounterSnapshot.Builder cacheEvictionTotal = CounterSnapshot.builder()
            .name("caffeine_cache_eviction")
            .help("Cache eviction totals, doesn't include manually removed entries");

        final GaugeSnapshot.Builder cacheEvictionWeight = GaugeSnapshot.builder()
            .name("caffeine_cache_eviction_weight")
            .help("Cache eviction weight");

        final CounterSnapshot.Builder cacheLoadFailure = CounterSnapshot.builder()
            .name("caffeine_cache_load_failure")
            .help("Cache load failures");

        final CounterSnapshot.Builder cacheLoadTotal = CounterSnapshot.builder()
            .name("caffeine_cache_loads")
            .help("Cache loads: both success and failures");

        final GaugeSnapshot.Builder cacheSize = GaugeSnapshot.builder()
            .name("caffeine_cache_estimated_size")
            .help("Estimated cache size");

        final SummarySnapshot.Builder cacheLoadSummary = SummarySnapshot.builder()
            .name("caffeine_cache_load_duration_seconds")
            .help("Cache load duration: both success and failures");

        for (final Map.Entry<String, Cache> c: children.entrySet()) {
            final List<String> cacheName = Arrays.asList(c.getKey());
            final Labels labels = Labels.of(labelNames, cacheName);

            final CacheStats stats = c.getValue().stats();

            try {
                cacheEvictionWeight.dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(labels)
                        .value(stats.evictionWeight())
                        .build()
                );
            } catch (Exception e) {
                // EvictionWeight metric is unavailable, newer version of Caffeine is needed.
            }

            cacheHitTotal.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(labels)
                    .value(stats.hitCount())
                    .build()
            );

            cacheMissTotal.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(labels)
                    .value(stats.missCount())
                    .build()
            );

            cacheRequestsTotal.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(labels)
                    .value(stats.requestCount())
                    .build()
            );

            cacheEvictionTotal.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(labels)
                    .value(stats.evictionCount())
                    .build()
            );

            cacheSize.dataPoint(
                GaugeSnapshot.GaugeDataPointSnapshot.builder()
                    .labels(labels)
                    .value(c.getValue().estimatedSize())
                    .build()
            );

            if (c.getValue() instanceof LoadingCache) {
                cacheLoadFailure.dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(labels)
                        .value(stats.loadFailureCount())
                        .build()
                );

                cacheLoadTotal.dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(labels)
                        .value(stats.loadCount())
                        .build()
                );

                cacheLoadSummary.dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .labels(labels)
                        .count(stats.loadCount())
                        .sum(stats.totalLoadTime() / NANOSECONDS_PER_SECOND)
                        .build()
                );
            }
        }

        return metricSnapshotsBuilder
            .metricSnapshot(cacheHitTotal.build())
            .metricSnapshot(cacheMissTotal.build())
            .metricSnapshot(cacheRequestsTotal.build())
            .metricSnapshot(cacheEvictionTotal.build())
            .metricSnapshot(cacheEvictionWeight.build())
            .metricSnapshot(cacheLoadFailure.build())
            .metricSnapshot(cacheLoadTotal.build())
            .metricSnapshot(cacheSize.build())
            .metricSnapshot(cacheLoadSummary.build())
            .build();
    }
}

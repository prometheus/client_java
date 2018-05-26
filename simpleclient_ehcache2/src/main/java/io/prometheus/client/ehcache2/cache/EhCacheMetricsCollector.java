package io.prometheus.client.ehcache2.cache;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.statistics.extended.ExtendedStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Collect metrics from Ehcache's v2 net.sf.ehcache.Ehcache.
 * <p>
 * <pre>{@code
 *
 * // Note need to set statistics="true" to the <cache> element of your ehcache configuration XML
 * org.springframework.cache.Cache ehcache2 = //get from Springs's context
 * org.springframework.cache.Cache otherEhcache2 = //get from Springs's context
 * CacheMetricsCollector cacheMetrics = new EhCacheMetricsCollector();
 * cacheMetrics.addCache("mycache", ehcache2);
 * cacheMetrics.addCache("othercache", otherEhcache2);
 * cacheMetrics.register()
 *
 * }</pre>
 * <p>
 * Exposed metrics are labeled with the provided cache name.
 * <p>
 * With the example above, sample metric names would be:
 * <pre>
 *     ehcache2_cache_hit_total{cache="mycache"} 10.0
 *     ehcache2_cache_hit_total{cache="othercache"} 87.0
 *     ehcache2_cache_miss_total{cache="mycache"} 3.0
 *     ehcache2_cache_miss_total{cache="othercache"} 55.0
 * </pre>
 */
public class EhCacheMetricsCollector extends Collector {

    protected final ConcurrentMap<String, Ehcache> children = new ConcurrentHashMap<String, Ehcache>();


    /**
     * Add or replace the cache with the given name.
     * <p>
     * Any references any previous cache with this name is invalidated.
     *
     * @param cacheName The name of the cache, will be the metrics label value
     * @param cache     The cache being monitored
     */
    public void addCache(String cacheName, Ehcache cache) {
        children.put(cacheName, cache);
    }

    /**
     * Remove the cache with the given name.
     * <p>
     * Any references to the cache are invalidated.
     *
     * @param cacheName cache to be removed
     */
    public Ehcache removeCache(String cacheName) {
        return children.remove(cacheName);
    }

    /**
     * Remove all caches.
     * <p>
     * Any references to all caches are invalidated.
     */
    public void clear() {
        children.clear();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        List<String> labelNames = Arrays.asList("cache");

        CounterMetricFamily cacheHitRatio = new CounterMetricFamily("ehcache2_cache_hit_ratio", "Cache hit ratio", labelNames);
        mfs.add(cacheHitRatio);
        CounterMetricFamily cacheHitTotal = new CounterMetricFamily("ehcache2_cache_hit_total", "Cache hit totals", labelNames);
        mfs.add(cacheHitTotal);

        CounterMetricFamily cacheEvictionTotal = new CounterMetricFamily("ehcache2_cache_eviction_total", "Cache eviction totals", labelNames);
        mfs.add(cacheEvictionTotal);

        CounterMetricFamily cacheInMemoryHitTotal = new CounterMetricFamily("ehcache2_cache_in_memory_hit_total", "Cache in-memory hit totals", labelNames);
        mfs.add(cacheInMemoryHitTotal);
        CounterMetricFamily cacheOffHeapHitTotal = new CounterMetricFamily("ehcache2_cache_off_heap_hit_total", "Cache off-heap hit totals", labelNames);
        mfs.add(cacheOffHeapHitTotal);
        CounterMetricFamily cacheOnDiskHitTotal = new CounterMetricFamily("ehcache2_cache_on_disk_hit_total", "Cache on-disk hit totals", labelNames);
        mfs.add(cacheOnDiskHitTotal);

        CounterMetricFamily cacheMissExpired = new CounterMetricFamily("ehcache2_cache_miss_expired", "Cache miss expired", labelNames);
        mfs.add(cacheMissExpired);
        CounterMetricFamily cacheMissTotal = new CounterMetricFamily("ehcache2_cache_miss_total", "Cache miss totals", labelNames);
        mfs.add(cacheMissTotal);
        CounterMetricFamily cacheInMemoryMissTotal = new CounterMetricFamily("ehcache2_cache_in_memory_miss_total", "Cache in-memory miss totals", labelNames);
        mfs.add(cacheInMemoryMissTotal);
        CounterMetricFamily cacheOffHeapMissTotal = new CounterMetricFamily("ehcache2_cache_off_heap_miss_total", "Cache off-heap miss totals", labelNames);
        mfs.add(cacheOffHeapMissTotal);
        CounterMetricFamily cacheOnDiskMissTotal = new CounterMetricFamily("ehcache2_cache_on_disk_miss_total", "Cache on-disk miss totals", labelNames);
        mfs.add(cacheOnDiskMissTotal);

        GaugeMetricFamily cacheSize = new GaugeMetricFamily("ehcache2_cache_size", "Cache size", labelNames);
        mfs.add(cacheSize);
        GaugeMetricFamily cacheInMemorySize = new GaugeMetricFamily("ehcache2_cache_in_memory_size", "Cache size", labelNames);
        mfs.add(cacheInMemorySize);
        GaugeMetricFamily cacheOffHeapSize = new GaugeMetricFamily("ehcache2_cache_off_heap_size", "Cache size", labelNames);
        mfs.add(cacheOffHeapSize);
        GaugeMetricFamily cacheOnDiskSize = new GaugeMetricFamily("ehcache2_cache_on_disk_size", "Cache size", labelNames);
        mfs.add(cacheOnDiskSize);

        GaugeMetricFamily cacheExpiredTotal = new GaugeMetricFamily("ehcache2_cache_expired_total", "Cache expired total", labelNames);
        mfs.add(cacheExpiredTotal);
        GaugeMetricFamily cacheEvictedTotal = new GaugeMetricFamily("ehcache2_cache_evicted_total", "Cache evicted total", labelNames);
        mfs.add(cacheEvictedTotal);

        GaugeMetricFamily cacheGetLatencyAvg = new GaugeMetricFamily("ehcache2_cache_get_latency_avg", "Cache average get latency ", labelNames);
        mfs.add(cacheGetLatencyAvg);

        GaugeMetricFamily cacheMissLatencyAvg = new GaugeMetricFamily("ehcache2_cache_miss_latency_avg", "Cache average miss latency ", labelNames);
        mfs.add(cacheMissLatencyAvg);

        GaugeMetricFamily cacheEvictionLatencyAvg = new GaugeMetricFamily("ehcache2_cache_eviction_latency_avg", "Cache average eviction latency ", labelNames);
        mfs.add(cacheEvictionLatencyAvg);

        GaugeMetricFamily cacheExpiredLatencyAvg = new GaugeMetricFamily("ehcache2_cache_expired_latency_avg", "Cache average expired latency ", labelNames);
        mfs.add(cacheExpiredLatencyAvg);

        GaugeMetricFamily cacheSearchLatencyAvg = new GaugeMetricFamily("ehcache2_cache_search_latency_avg", "Cache average search latency ", labelNames);
        mfs.add(cacheSearchLatencyAvg);

        GaugeMetricFamily cacheSearchPerSecond = new GaugeMetricFamily("ehcache2_cache_search_per_second", "Cache search per second ", labelNames);
        mfs.add(cacheSearchPerSecond);
        GaugeMetricFamily cacheGetPerSecond = new GaugeMetricFamily("ehcache2_cache_get_per_second", "Cache get per second ", labelNames);
        mfs.add(cacheGetPerSecond);

        GaugeMetricFamily cacheHeapSizeInBytes = new GaugeMetricFamily("ehcache2_cache_heap_memory_bytes", "Used memory of cache in heap in bytes", labelNames);
        mfs.add(cacheHeapSizeInBytes);
        GaugeMetricFamily cacheOffHeapSizeInBytes = new GaugeMetricFamily("ehcache2_cache_off_heap_memory_bytes", "Used memory of cache in off-heap in bytes ", labelNames);
        mfs.add(cacheOffHeapSizeInBytes);
        GaugeMetricFamily cacheDiskSizeInBytes = new GaugeMetricFamily("ehcache2_cache_disk_memory_bytes", "Used memory of cache in disk in bytes", labelNames);
        mfs.add(cacheDiskSizeInBytes);

        GaugeMetricFamily cacheWriterQueueLength = new GaugeMetricFamily("ehcache2_cache_writer_queue_length", "Cache writer queue length", labelNames);
        mfs.add(cacheWriterQueueLength);

        for (Map.Entry<String, Ehcache> c : children.entrySet()) {
            List<String> cacheName = Arrays.asList(c.getKey());
            StatisticsGateway stats = c.getValue().getStatistics();

            cacheHitRatio.addMetric(cacheName, stats.cacheHitRatio());
            cacheHitTotal.addMetric(cacheName, stats.cacheHitCount());
            cacheEvictionTotal.addMetric(cacheName, stats.cacheEvictedCount());
            cacheInMemoryHitTotal.addMetric(cacheName, stats.localHeapHitCount());
            cacheOffHeapHitTotal.addMetric(cacheName, stats.localOffHeapHitCount());
            cacheOnDiskHitTotal.addMetric(cacheName, stats.localDiskHitCount());

            cacheMissExpired.addMetric(cacheName, stats.cacheMissExpiredCount());
            cacheMissTotal.addMetric(cacheName, stats.cacheMissCount());
            cacheInMemoryMissTotal.addMetric(cacheName, stats.localHeapMissCount());
            cacheOffHeapMissTotal.addMetric(cacheName, stats.localOffHeapMissCount());
            cacheOnDiskMissTotal.addMetric(cacheName, stats.localDiskMissCount());

            cacheSize.addMetric(cacheName, stats.getSize());
            cacheInMemorySize.addMetric(cacheName, stats.getLocalHeapSize());
            cacheOffHeapSize.addMetric(cacheName, stats.getLocalOffHeapSize());
            cacheOnDiskSize.addMetric(cacheName, stats.getLocalDiskSize());

            cacheExpiredTotal.addMetric(cacheName, stats.cacheExpiredCount());
            cacheEvictedTotal.addMetric(cacheName, stats.cacheEvictedCount());

            ExtendedStatistics.Result cacheGetOperation = stats.cacheGetOperation();
            ExtendedStatistics.Result cacheSearchOperation = stats.cacheSearchOperation();
            ExtendedStatistics.Result cacheMissOperation = stats.cacheMissOperation();
            ExtendedStatistics.Result cacheEvictionOperation = stats.cacheEvictionOperation();
            ExtendedStatistics.Result cacheExpiredOperation = stats.cacheExpiredOperation();

            cacheGetLatencyAvg.addMetric(cacheName, cacheGetOperation.latency().average().value());

            cacheMissLatencyAvg.addMetric(cacheName, cacheMissOperation.latency().average().value());

            cacheEvictionLatencyAvg.addMetric(cacheName, cacheEvictionOperation.latency().average().value());

            cacheExpiredLatencyAvg.addMetric(cacheName, cacheExpiredOperation.latency().average().value());

            cacheSearchLatencyAvg.addMetric(cacheName, cacheSearchOperation.latency().average().value());

            cacheGetPerSecond.addMetric(cacheName, cacheGetOperation.rate().value());
            cacheSearchPerSecond.addMetric(cacheName, cacheSearchOperation.rate().value());

            cacheHeapSizeInBytes.addMetric(cacheName, stats.getLocalHeapSizeInBytes());
            cacheOffHeapSizeInBytes.addMetric(cacheName, stats.getLocalOffHeapSizeInBytes());
            cacheDiskSizeInBytes.addMetric(cacheName, stats.getLocalDiskSizeInBytes());

            cacheWriterQueueLength.addMetric(cacheName, stats.getWriterQueueLength());
        }
        return mfs;
    }
}
package io.prometheus.client.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.Summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Collect metrics from Guava's com.google.common.cache.Cache.
 * <p>
 * <pre>{@code
 *
 * // Note that `recordStats()` is required to gather non-zero statistics
 * Cache<String, String> cache = CacheBuilder.newBuilder().recordStats().build();
 * new CacheMetricsCollector(cache, "myapp_mycache", "Names cache").register();
 *
 * }</pre>
 *
 * Exposed metrics are prefixed with the provided prefix.
 *
 * With the examle above sample metric names would be:
 * <pre>
 *     myapp_mycache_cache_request_total{found="hit",} 10.0
 *     myapp_mycache_cache_request_total{found="miss",} 3.0
 *     myapp_mycache_cache_eviction_total 1.0
 * </pre>
 *
 * Additionally if the cache includes a loader additional metrics would be provided:
 * <pre>
 *     myapp_mycache_cache_load_total{success="success",} 5.0
 *     myapp_mycache_cache_load_total{success="exception",} 1.0
 *     myapp_mycache_cache_load_sum_seconds 0.0034
 * </pre>
 *
 */
public class CacheMetricsCollector extends Collector {

    private final Cache cache;
    private final String cacheName;
    private final String help;

    public CacheMetricsCollector(Cache cache, String cacheName, String help) {
        this.cache = cache;
        this.cacheName = cacheName;
        this.help = help;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        CacheStats stats = cache.stats();

        CounterMetricFamily cacheRequestTotal = new CounterMetricFamily(cacheName+"_cache_request_total",
                help + " cache request totals", Arrays.asList("found"));
        mfs.add(cacheRequestTotal);
        cacheRequestTotal.addMetric(Arrays.asList("hit"), stats.hitCount());
        cacheRequestTotal.addMetric(Arrays.asList("miss"), stats.missCount());

        mfs.add(new CounterMetricFamily(cacheName+"_cache_eviction_total",
                help + " cache eviction totals", stats.evictionCount()));

        if(cache instanceof LoadingCache) {
            CounterMetricFamily cacheLoadTotal = new CounterMetricFamily(cacheName+"_cache_load_total",
                    help + " cache load totals", Arrays.asList("success"));
            mfs.add(cacheLoadTotal);
            cacheLoadTotal.addMetric(Arrays.asList("success"), stats.loadSuccessCount());
            cacheLoadTotal.addMetric(Arrays.asList("exception"), stats.loadExceptionCount());

            mfs.add(new CounterMetricFamily(cacheName+"_cache_load_sum_seconds",
                    help + " cache load total time in seconds", stats.totalLoadTime()/ Summary.NANOSECONDS_PER_SECOND));
        }

        return mfs;
    }
}

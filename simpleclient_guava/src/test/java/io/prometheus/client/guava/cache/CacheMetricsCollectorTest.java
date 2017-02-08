package io.prometheus.client.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;


import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheMetricsCollectorTest {

    @Test
    public void cacheExposesMetricsForHitMissAndEviction() throws Exception {
        Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(2).recordStats().build();
        CollectorRegistry registry = new CollectorRegistry();

        CacheMetricsCollector collector = new CacheMetricsCollector().register(registry);
        collector.addCache("users", cache);

        cache.getIfPresent("user1");
        cache.getIfPresent("user1");
        cache.put("user1", "First User");
        cache.getIfPresent("user1");

        // Add to cache to trigger eviction.
        cache.put("user2", "Second User");
        cache.put("user3", "Third User");
        cache.put("user4", "Fourth User");

        assertMetric(registry, "guava_cache_hit_total", "users", 1.0);
        assertMetric(registry, "guava_cache_miss_total", "users", 2.0);
        assertMetric(registry, "guava_cache_requests_total", "users", 3.0);
        assertMetric(registry, "guava_cache_eviction_total", "users", 2.0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadingCacheExposesMetricsForLoadsAndExceptions() throws Exception {
        CacheLoader<String, String> loader = mock(CacheLoader.class);
        when(loader.load(anyString()))
                .thenReturn("First User")
                .thenThrow(new RuntimeException("Seconds time fails"))
                .thenReturn("Third User");

        LoadingCache<String, String> cache = CacheBuilder.newBuilder().recordStats().build(loader);
        CollectorRegistry registry = new CollectorRegistry();
        CacheMetricsCollector collector = new CacheMetricsCollector().register(registry);
        collector.addCache("loadingusers", cache);

        cache.get("user1");
        cache.get("user1");
        try{
            cache.get("user2");
        } catch (Exception e) {
            // ignoring.
        }
        cache.get("user3");

        assertMetric(registry, "guava_cache_hit_total", "loadingusers", 1.0);
        assertMetric(registry, "guava_cache_miss_total", "loadingusers", 3.0);

        assertMetric(registry, "guava_cache_load_failure_total", "loadingusers", 1.0);
        assertMetric(registry, "guava_cache_loads_total", "loadingusers", 3.0);

        assertMetric(registry, "guava_cache_load_duration_seconds_count", "loadingusers", 3.0);
        assertMetricGreatThan(registry, "guava_cache_load_duration_seconds_sum", "loadingusers", 0.0);
    }


    private void assertMetric(CollectorRegistry registry, String name, String cacheName, double value) {
        assertThat(registry.getSampleValue(name, new String[]{"cache"}, new String[]{cacheName})).isEqualTo(value);
    }


    private void assertMetricGreatThan(CollectorRegistry registry, String name, String cacheName, double value) {
        assertThat(registry.getSampleValue(name, new String[]{"cache"}, new String[]{cacheName})).isGreaterThan(value);
    }


}

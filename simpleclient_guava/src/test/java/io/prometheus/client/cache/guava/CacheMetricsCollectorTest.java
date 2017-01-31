package io.prometheus.client.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;


import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheMetricsCollectorTest {

    @Test
    public void cacheExposesMetricsForHitMissAndEviction() throws Exception {
        Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(2).recordStats().build();
        CacheMetricsCollector collector = new CacheMetricsCollector(cache, "myapp_users", "Names cache");

        cache.getIfPresent("user1");
        cache.getIfPresent("user1");
        cache.put("user1", "First User");
        cache.getIfPresent("user1");

        //Add to cache to trigger eviction
        cache.put("user2", "Second User");
        cache.put("user3", "Third User");
        cache.put("user4", "Fourth User");

        CollectorRegistry registry = new CollectorRegistry();
        collector.register(registry);

        assertThat(registry.getSampleValue("myapp_users_cache_request_total", new String[]{"found"},
                new String[]{"hit"})).isEqualTo(1.0);
        assertThat(registry.getSampleValue("myapp_users_cache_request_total",
                new String[]{"found"}, new String[]{"miss"})).isEqualTo(2.0);
        assertThat(registry.getSampleValue("myapp_users_cache_eviction_total")).isEqualTo(2.0);
    }

    @Test
    public void loadingCacheExposesMetricsForLoadsAndExceptions() throws Exception {
        CacheLoader<String, String> loader = mock(CacheLoader.class);
        when(loader.load(anyString()))
                .thenReturn("First User")
                .thenThrow(new RuntimeException("Seconds time fails"))
                .thenReturn("Third User");

        LoadingCache<String, String> cache = CacheBuilder.newBuilder().recordStats().build(loader);
        CacheMetricsCollector collector = new CacheMetricsCollector(cache, "myapp_loadingusers", "Names cache via loader");

        cache.get("user1");
        cache.get("user1");
        try{
            cache.get("user2");
        } catch (Exception e) {
            //ignoring
        }
        cache.get("user3");

        CollectorRegistry registry = new CollectorRegistry();
        collector.register(registry);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_request_total", new String[]{"found"},
                new String[]{"hit"})).isEqualTo(1.0);
        assertThat(registry.getSampleValue("myapp_loadingusers_cache_request_total",
                new String[]{"found"}, new String[]{"miss"})).isEqualTo(3.0);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_total",
                new String[]{"success"}, new String[]{"success"})).isEqualTo(2.0);
        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_total",
                new String[]{"success"}, new String[]{"exception"})).isEqualTo(1.0);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_sum_seconds")).isGreaterThan(0.0);
    }


}

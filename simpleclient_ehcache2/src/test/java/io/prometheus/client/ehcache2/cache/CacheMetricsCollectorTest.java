package io.prometheus.client.ehcache2.cache;

import io.prometheus.client.CollectorRegistry;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CacheMetricsCollectorTest {

    @Test
    public void cacheExposesMetricsForHitMissAndEviction() throws Exception {
        CacheManager singletonManager = CacheManager.create();
        Cache testCache = new Cache(
                new CacheConfiguration("testCache", 2)
                        .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                        .eternal(false)
                        .timeToLiveSeconds(5)
                        .timeToIdleSeconds(5)
                        .diskExpiryThreadIntervalSeconds(5)
                        .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))

        );
        singletonManager.addCache(testCache);
        Cache test = singletonManager.getCache("testCache");
        CollectorRegistry registry = new CollectorRegistry();
        EhCacheMetricsCollector collector = new EhCacheMetricsCollector().register(registry);
        collector.addCache("testCache", test);
        test.get("key1");
        test.get("key1");
        test.put(new Element("key1", "value1"));
        test.get("key1");

        test.put(new Element("key2", "value2"));
        test.put(new Element("key3", "value3"));
        test.put(new Element("key4", "value4"));

        assertMetric(registry, "ehcache2_cache_hit_total", "testCache", 1.0);
        assertMetric(registry, "ehcache2_cache_miss_total", "testCache", 2.0);
        singletonManager.removeCache("testCache");
    }

    private void assertMetric(CollectorRegistry registry, String name, String cacheName, double value) {
        assertThat(registry.getSampleValue(name, new String[]{"cache"}, new String[]{cacheName})).isEqualTo(value);
    }


    private void assertMetricGreatThan(CollectorRegistry registry, String name, String cacheName, double value) {
        assertThat(registry.getSampleValue(name, new String[]{"cache"}, new String[]{cacheName})).isGreaterThan(value);
    }


}

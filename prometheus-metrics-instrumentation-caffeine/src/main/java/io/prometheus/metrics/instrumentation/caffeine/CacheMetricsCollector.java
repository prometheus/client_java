package io.prometheus.metrics.instrumentation.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Collect metrics from Caffeine's com.github.benmanes.caffeine.cache.Cache.
 *
 * <p>
 *
 * <pre>{@code
 * // Note that `recordStats()` is required to gather non-zero statistics
 * Cache<String, String> cache = Caffeine.newBuilder().recordStats().build();
 * CacheMetricsCollector cacheMetrics = CacheMetricsCollector.builder().build();
 * PrometheusRegistry.defaultRegistry.register(cacheMetrics);
 * cacheMetrics.addCache("mycache", cache);
 *
 * }</pre>
 *
 * Exposed metrics are labeled with the provided cache name.
 *
 * <p>With the example above, sample metric names would be:
 *
 * <pre>
 *     caffeine_cache_hit_total{cache="mycache"} 10.0
 *     caffeine_cache_miss_total{cache="mycache"} 3.0
 *     caffeine_cache_requests_total{cache="mycache"} 13.0
 *     caffeine_cache_eviction_total{cache="mycache"} 1.0
 *     caffeine_cache_estimated_size{cache="mycache"} 5.0
 * </pre>
 *
 * Additionally, if the cache includes a loader, the following metrics would be provided:
 *
 * <pre>
 *     caffeine_cache_load_failure_total{cache="mycache"} 2.0
 *     caffeine_cache_loads_total{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_count{cache="mycache"} 7.0
 *     caffeine_cache_load_duration_seconds_sum{cache="mycache"} 0.0034
 * </pre>
 */
public class CacheMetricsCollector implements MultiCollector {
  private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;

  private static final String METRIC_NAME_CACHE_HIT = "caffeine_cache_hit";
  private static final String METRIC_NAME_CACHE_MISS = "caffeine_cache_miss";
  private static final String METRIC_NAME_CACHE_REQUESTS = "caffeine_cache_requests";
  private static final String METRIC_NAME_CACHE_EVICTION = "caffeine_cache_eviction";
  private static final String METRIC_NAME_CACHE_EVICTION_WEIGHT = "caffeine_cache_eviction_weight";
  private static final String METRIC_NAME_CACHE_LOAD_FAILURE = "caffeine_cache_load_failure";
  private static final String METRIC_NAME_CACHE_LOADS = "caffeine_cache_loads";
  private static final String METRIC_NAME_CACHE_ESTIMATED_SIZE = "caffeine_cache_estimated_size";
  private static final String METRIC_NAME_CACHE_WEIGHTED_SIZE = "caffeine_cache_weighted_size";
  private static final String METRIC_NAME_CACHE_LOAD_DURATION_SECONDS =
      "caffeine_cache_load_duration_seconds";

  private static final List<String> ALL_METRIC_NAMES =
      Collections.unmodifiableList(
          Arrays.asList(
              METRIC_NAME_CACHE_HIT,
              METRIC_NAME_CACHE_MISS,
              METRIC_NAME_CACHE_REQUESTS,
              METRIC_NAME_CACHE_EVICTION,
              METRIC_NAME_CACHE_EVICTION_WEIGHT,
              METRIC_NAME_CACHE_LOAD_FAILURE,
              METRIC_NAME_CACHE_LOADS,
              METRIC_NAME_CACHE_ESTIMATED_SIZE,
              METRIC_NAME_CACHE_WEIGHTED_SIZE,
              METRIC_NAME_CACHE_LOAD_DURATION_SECONDS));

  protected final ConcurrentMap<String, Cache<?, ?>> children = new ConcurrentHashMap<>();
  private final boolean collectEvictionWeightAsCounter;

  /**
   * Instantiates a {@link CacheMetricsCollector}, with the legacy parameters.
   *
   * <p>The use of this constructor is discouraged, in favor of a Builder pattern {@link #builder()}
   *
   * <p>Note that the {@link #builder()} API has different default values than this deprecated
   * constructor.
   */
  @Deprecated
  public CacheMetricsCollector() {
    this(false);
  }

  /**
   * Instantiate a {@link CacheMetricsCollector}
   *
   * @param collectEvictionWeightAsCounter If true, {@code caffeine_cache_eviction_weight} will be
   *     observed as an incrementing counter instead of a gauge.
   */
  protected CacheMetricsCollector(boolean collectEvictionWeightAsCounter) {
    this.collectEvictionWeightAsCounter = collectEvictionWeightAsCounter;
  }

  /**
   * Add or replace the cache with the given name.
   *
   * <p>Any references any previous cache with this name is invalidated.
   *
   * @param cacheName The name of the cache, will be the metrics label value
   * @param cache The cache being monitored
   */
  public void addCache(String cacheName, Cache<?, ?> cache) {
    children.put(cacheName, cache);
  }

  /**
   * Add or replace the cache with the given name.
   *
   * <p>Any references any previous cache with this name is invalidated.
   *
   * @param cacheName The name of the cache, will be the metrics label value
   * @param cache The cache being monitored
   */
  public void addCache(String cacheName, AsyncCache<?, ?> cache) {
    children.put(cacheName, cache.synchronous());
  }

  /**
   * Remove the cache with the given name.
   *
   * <p>Any references to the cache are invalidated.
   *
   * @param cacheName cache to be removed
   */
  public Cache<?, ?> removeCache(String cacheName) {
    return children.remove(cacheName);
  }

  /**
   * Remove all caches.
   *
   * <p>Any references to all caches are invalidated.
   */
  public void clear() {
    children.clear();
  }

  @Override
  public MetricSnapshots collect() {
    final MetricSnapshots.Builder metricSnapshotsBuilder = MetricSnapshots.builder();
    final List<String> labelNames = Arrays.asList("cache");

    final CounterSnapshot.Builder cacheHitTotal =
        CounterSnapshot.builder().name(METRIC_NAME_CACHE_HIT).help("Cache hit totals");

    final CounterSnapshot.Builder cacheMissTotal =
        CounterSnapshot.builder().name(METRIC_NAME_CACHE_MISS).help("Cache miss totals");

    final CounterSnapshot.Builder cacheRequestsTotal =
        CounterSnapshot.builder()
            .name(METRIC_NAME_CACHE_REQUESTS)
            .help("Cache request totals, hits + misses");

    final CounterSnapshot.Builder cacheEvictionTotal =
        CounterSnapshot.builder()
            .name(METRIC_NAME_CACHE_EVICTION)
            .help("Cache eviction totals, doesn't include manually removed entries");

    final CounterSnapshot.Builder cacheEvictionWeight =
        CounterSnapshot.builder()
            .name(METRIC_NAME_CACHE_EVICTION_WEIGHT)
            .help("Weight of evicted cache entries, doesn't include manually removed entries");
    final GaugeSnapshot.Builder cacheEvictionWeightLegacyGauge =
        GaugeSnapshot.builder()
            .name(METRIC_NAME_CACHE_EVICTION_WEIGHT)
            .help("Weight of evicted cache entries, doesn't include manually removed entries");

    final CounterSnapshot.Builder cacheLoadFailure =
        CounterSnapshot.builder().name(METRIC_NAME_CACHE_LOAD_FAILURE).help("Cache load failures");

    final CounterSnapshot.Builder cacheLoadTotal =
        CounterSnapshot.builder()
            .name(METRIC_NAME_CACHE_LOADS)
            .help("Cache loads: both success and failures");

    final GaugeSnapshot.Builder cacheSize =
        GaugeSnapshot.builder().name(METRIC_NAME_CACHE_ESTIMATED_SIZE).help("Estimated cache size");

    final GaugeSnapshot.Builder cacheWeightedSize =
        GaugeSnapshot.builder()
            .name(METRIC_NAME_CACHE_WEIGHTED_SIZE)
            .help("Approximate accumulated weight of cache entries");

    final SummarySnapshot.Builder cacheLoadSummary =
        SummarySnapshot.builder()
            .name(METRIC_NAME_CACHE_LOAD_DURATION_SECONDS)
            .help("Cache load duration: both success and failures");

    for (final Map.Entry<String, Cache<?, ?>> c : children.entrySet()) {
      final List<String> cacheName = Collections.singletonList(c.getKey());
      final Labels labels = Labels.of(labelNames, cacheName);

      final CacheStats stats = c.getValue().stats();

      try {
        cacheEvictionWeight.dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(labels)
                .value(stats.evictionWeight())
                .build());
        cacheEvictionWeightLegacyGauge.dataPoint(
            GaugeSnapshot.GaugeDataPointSnapshot.builder()
                .labels(labels)
                .value(stats.evictionWeight())
                .build());
      } catch (Exception e) {
        // EvictionWeight metric is unavailable, newer version of Caffeine is needed.
      }

      final Optional<? extends Policy.Eviction<?, ?>> eviction = c.getValue().policy().eviction();
      if (eviction.isPresent() && eviction.get().weightedSize().isPresent()) {
        cacheWeightedSize.dataPoint(
            GaugeSnapshot.GaugeDataPointSnapshot.builder()
                .labels(labels)
                .value(eviction.get().weightedSize().getAsLong())
                .build());
      }

      cacheHitTotal.dataPoint(
          CounterSnapshot.CounterDataPointSnapshot.builder()
              .labels(labels)
              .value(stats.hitCount())
              .build());

      cacheMissTotal.dataPoint(
          CounterSnapshot.CounterDataPointSnapshot.builder()
              .labels(labels)
              .value(stats.missCount())
              .build());

      cacheRequestsTotal.dataPoint(
          CounterSnapshot.CounterDataPointSnapshot.builder()
              .labels(labels)
              .value(stats.requestCount())
              .build());

      cacheEvictionTotal.dataPoint(
          CounterSnapshot.CounterDataPointSnapshot.builder()
              .labels(labels)
              .value(stats.evictionCount())
              .build());

      cacheSize.dataPoint(
          GaugeSnapshot.GaugeDataPointSnapshot.builder()
              .labels(labels)
              .value(c.getValue().estimatedSize())
              .build());

      if (c.getValue() instanceof LoadingCache) {
        cacheLoadFailure.dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(labels)
                .value(stats.loadFailureCount())
                .build());

        cacheLoadTotal.dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(labels)
                .value(stats.loadCount())
                .build());

        cacheLoadSummary.dataPoint(
            SummarySnapshot.SummaryDataPointSnapshot.builder()
                .labels(labels)
                .count(stats.loadCount())
                .sum(stats.totalLoadTime() / NANOSECONDS_PER_SECOND)
                .build());
      }
    }

    return metricSnapshotsBuilder
        .metricSnapshot(cacheHitTotal.build())
        .metricSnapshot(cacheMissTotal.build())
        .metricSnapshot(cacheRequestsTotal.build())
        .metricSnapshot(cacheEvictionTotal.build())
        .metricSnapshot(
            collectEvictionWeightAsCounter
                ? cacheEvictionWeight.build()
                : cacheEvictionWeightLegacyGauge.build())
        .metricSnapshot(cacheLoadFailure.build())
        .metricSnapshot(cacheLoadTotal.build())
        .metricSnapshot(cacheSize.build())
        .metricSnapshot(cacheWeightedSize.build())
        .metricSnapshot(cacheLoadSummary.build())
        .build();
  }

  @Override
  public List<String> getPrometheusNames() {
    return ALL_METRIC_NAMES;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private boolean collectEvictionWeightAsCounter = true;

    public Builder collectEvictionWeightAsCounter(boolean collectEvictionWeightAsCounter) {
      this.collectEvictionWeightAsCounter = collectEvictionWeightAsCounter;
      return this;
    }

    public CacheMetricsCollector build() {
      return new CacheMetricsCollector(collectEvictionWeightAsCounter);
    }
  }
}

package io.prometheus.metrics.instrumentation.caffeine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@SuppressWarnings("CheckReturnValue")
class CacheMetricsCollectorTest {
  // This enum was added to simplify test parametrization on argument options.
  public enum Options {
    LEGACY(false, false),
    COLLECT_EVICTION_WEIGHT_AS_COUNTER(true, false),
    COLLECT_WEIGHTED_SIZE(false, true),
    BUILDER_DEFAULT(true, true);

    private final boolean collectEvictionWeightAsCounter;
    private final boolean collectWeightedSize;

    Options(boolean collectEvictionWeightAsCounter, boolean collectWeightedSize) {
      this.collectEvictionWeightAsCounter = collectEvictionWeightAsCounter;
      this.collectWeightedSize = collectWeightedSize;
    }
  }

  @ParameterizedTest
  @EnumSource
  public void cacheExposesMetricsForHitMissAndEviction(Options options) {
    // Run cleanup in same thread, to remove async behavior with evictions
    final Cache<String, String> cache =
        Caffeine.newBuilder().maximumSize(2).recordStats().executor(Runnable::run).build();

    final CacheMetricsCollector collector =
        CacheMetricsCollector.builder()
            .collectEvictionWeightAsCounter(options.collectEvictionWeightAsCounter)
            .collectWeightedSize(options.collectWeightedSize)
            .build();
    collector.addCache("users", cache);

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    cache.getIfPresent("user1");
    cache.getIfPresent("user1");
    cache.put("user1", "First User");
    cache.getIfPresent("user1");

    // Add to cache to trigger eviction.
    cache.put("user2", "Second User");
    cache.put("user3", "Third User");
    cache.put("user4", "Fourth User");

    assertCounterMetric(registry, "caffeine_cache_hit", "users", 1.0);
    assertCounterMetric(registry, "caffeine_cache_miss", "users", 2.0);
    assertCounterMetric(registry, "caffeine_cache_requests", "users", 3.0);
    assertCounterMetric(registry, "caffeine_cache_eviction", "users", 2.0);
    String openMetricEvictionWeightExpectedText;
    if (options.collectEvictionWeightAsCounter) {
      assertCounterMetric(registry, "caffeine_cache_eviction_weight", "users", 2.0);
      openMetricEvictionWeightExpectedText =
          "# TYPE caffeine_cache_eviction_weight counter\n"
              + "# HELP caffeine_cache_eviction_weight Weight of evicted cache entries, doesn't include manually removed entries\n"
              + "caffeine_cache_eviction_weight_total{cache=\"users\"} 2.0\n";
    } else {
      assertGaugeMetric(registry, "caffeine_cache_eviction_weight", "users", 2.0);
      openMetricEvictionWeightExpectedText =
          "# TYPE caffeine_cache_eviction_weight gauge\n"
              + "# HELP caffeine_cache_eviction_weight Weight of evicted cache entries, doesn't include manually removed entries\n"
              + "caffeine_cache_eviction_weight{cache=\"users\"} 2.0\n";
    }

    final String expected =
        "# TYPE caffeine_cache_estimated_size gauge\n"
            + "# HELP caffeine_cache_estimated_size Estimated cache size\n"
            + "caffeine_cache_estimated_size{cache=\"users\"} 2.0\n"
            + "# TYPE caffeine_cache_eviction counter\n"
            + "# HELP caffeine_cache_eviction Cache eviction totals, doesn't include manually removed entries\n"
            + "caffeine_cache_eviction_total{cache=\"users\"} 2.0\n"
            + openMetricEvictionWeightExpectedText
            + "# TYPE caffeine_cache_hit counter\n"
            + "# HELP caffeine_cache_hit Cache hit totals\n"
            + "caffeine_cache_hit_total{cache=\"users\"} 1.0\n"
            + "# TYPE caffeine_cache_miss counter\n"
            + "# HELP caffeine_cache_miss Cache miss totals\n"
            + "caffeine_cache_miss_total{cache=\"users\"} 2.0\n"
            + "# TYPE caffeine_cache_requests counter\n"
            + "# HELP caffeine_cache_requests Cache request totals, hits + misses\n"
            + "caffeine_cache_requests_total{cache=\"users\"} 3.0\n"
            + "# EOF\n";

    assertThat(convertToOpenMetricsFormat(registry)).isEqualTo(expected);
  }

  @ParameterizedTest
  @EnumSource
  public void weightedCacheExposesMetricsForHitMissAndEvictionWeightedSize(Options options) {
    // Run cleanup in same thread, to remove async behavior with evictions
    final Cache<String, String> cache =
        Caffeine.newBuilder()
            .weigher((String k, String v) -> k.length() + v.length())
            .maximumWeight(35)
            .recordStats()
            .executor(Runnable::run)
            .build();

    final CacheMetricsCollector collector =
        CacheMetricsCollector.builder()
            .collectEvictionWeightAsCounter(options.collectEvictionWeightAsCounter)
            .collectWeightedSize(options.collectWeightedSize)
            .build();
    collector.addCache("users", cache);

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    cache.getIfPresent("user1");
    cache.getIfPresent("user1");
    cache.put("user1", "First User");
    cache.getIfPresent("user1");

    // Add to cache to trigger eviction.
    cache.put("user2", "Second User");
    cache.put("user3", "Third User");
    cache.put("user4", "Fourth User");

    assertCounterMetric(registry, "caffeine_cache_hit", "users", 1.0);
    assertCounterMetric(registry, "caffeine_cache_miss", "users", 2.0);
    assertCounterMetric(registry, "caffeine_cache_requests", "users", 3.0);
    assertCounterMetric(registry, "caffeine_cache_eviction", "users", 2.0);
    String openMetricEvictionWeightExpectedText;
    if (options.collectEvictionWeightAsCounter) {
      assertCounterMetric(registry, "caffeine_cache_eviction_weight", "users", 31.0);
      openMetricEvictionWeightExpectedText =
          "# TYPE caffeine_cache_eviction_weight counter\n"
              + "# HELP caffeine_cache_eviction_weight Weight of evicted cache entries, doesn't include manually removed entries\n"
              + "caffeine_cache_eviction_weight_total{cache=\"users\"} 31.0\n";
    } else {
      assertGaugeMetric(registry, "caffeine_cache_eviction_weight", "users", 31.0);
      openMetricEvictionWeightExpectedText =
          "# TYPE caffeine_cache_eviction_weight gauge\n"
              + "# HELP caffeine_cache_eviction_weight Weight of evicted cache entries, doesn't include manually removed entries\n"
              + "caffeine_cache_eviction_weight{cache=\"users\"} 31.0\n";
    }
    String openMetricWeightedSizeExpectedText;
    if (options.collectWeightedSize) {
      openMetricWeightedSizeExpectedText =
          "# TYPE caffeine_cache_weighted_size gauge\n"
              + "# HELP caffeine_cache_weighted_size Approximate accumulated weight of cache entries\n"
              + "caffeine_cache_weighted_size{cache=\"users\"} 31.0\n";
    } else {
      openMetricWeightedSizeExpectedText = "";
    }

    final String expected =
        "# TYPE caffeine_cache_estimated_size gauge\n"
            + "# HELP caffeine_cache_estimated_size Estimated cache size\n"
            + "caffeine_cache_estimated_size{cache=\"users\"} 2.0\n"
            + "# TYPE caffeine_cache_eviction counter\n"
            + "# HELP caffeine_cache_eviction Cache eviction totals, doesn't include manually removed entries\n"
            + "caffeine_cache_eviction_total{cache=\"users\"} 2.0\n"
            + openMetricEvictionWeightExpectedText
            + "# TYPE caffeine_cache_hit counter\n"
            + "# HELP caffeine_cache_hit Cache hit totals\n"
            + "caffeine_cache_hit_total{cache=\"users\"} 1.0\n"
            + "# TYPE caffeine_cache_miss counter\n"
            + "# HELP caffeine_cache_miss Cache miss totals\n"
            + "caffeine_cache_miss_total{cache=\"users\"} 2.0\n"
            + "# TYPE caffeine_cache_requests counter\n"
            + "# HELP caffeine_cache_requests Cache request totals, hits + misses\n"
            + "caffeine_cache_requests_total{cache=\"users\"} 3.0\n"
            + openMetricWeightedSizeExpectedText
            + "# EOF\n";

    assertThat(convertToOpenMetricsFormat(registry)).isEqualTo(expected);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void loadingCacheExposesMetricsForLoadsAndExceptions() throws Exception {
    final CacheLoader<String, String> loader = mock(CacheLoader.class);
    when(loader.load(anyString()))
        .thenReturn("First User")
        .thenThrow(new RuntimeException("Seconds time fails"))
        .thenReturn("Third User");

    final LoadingCache<String, String> cache = Caffeine.newBuilder().recordStats().build(loader);
    final CacheMetricsCollector collector = CacheMetricsCollector.builder().build();

    collector.addCache("loadingusers", cache);

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    cache.get("user1");
    cache.get("user1");
    try {
      cache.get("user2");
    } catch (Exception e) {
      // ignoring.
    }
    cache.get("user3");

    assertCounterMetric(registry, "caffeine_cache_hit", "loadingusers", 1.0);
    assertCounterMetric(registry, "caffeine_cache_miss", "loadingusers", 3.0);

    assertCounterMetric(registry, "caffeine_cache_load_failure", "loadingusers", 1.0);
    assertCounterMetric(registry, "caffeine_cache_loads", "loadingusers", 3.0);

    final SummarySnapshot.SummaryDataPointSnapshot loadDuration =
        (SummarySnapshot.SummaryDataPointSnapshot)
            getDataPointSnapshot(registry, "caffeine_cache_load_duration_seconds", "loadingusers");

    assertThat(loadDuration.getCount()).isEqualTo(3);
    assertThat(loadDuration.getSum()).isGreaterThan(0);
  }

  @ParameterizedTest
  @EnumSource
  public void getPrometheusNamesHasSameSizeAsMetricSizeWhenScraping(Options options) {
    final CacheMetricsCollector collector =
        CacheMetricsCollector.builder()
            .collectEvictionWeightAsCounter(options.collectEvictionWeightAsCounter)
            .collectWeightedSize(options.collectWeightedSize)
            .build();

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    final MetricSnapshots metricSnapshots = registry.scrape();
    final List<String> prometheusNames = collector.getPrometheusNames();

    assertThat(prometheusNames).hasSize(metricSnapshots.size());
  }

  @ParameterizedTest
  @EnumSource
  public void collectedMetricNamesAreKnownPrometheusNames(Options options) {
    final CacheMetricsCollector collector =
        CacheMetricsCollector.builder()
            .collectEvictionWeightAsCounter(options.collectEvictionWeightAsCounter)
            .collectWeightedSize(options.collectWeightedSize)
            .build();

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    final MetricSnapshots metricSnapshots = registry.scrape();
    final List<String> prometheusNames = collector.getPrometheusNames();

    metricSnapshots.forEach(
        metricSnapshot ->
            assertThat(prometheusNames).contains(metricSnapshot.getMetadata().getPrometheusName()));
  }

  private void assertCounterMetric(
      PrometheusRegistry registry, String name, String cacheName, double value) {
    final CounterSnapshot.CounterDataPointSnapshot dataPointSnapshot =
        (CounterSnapshot.CounterDataPointSnapshot) getDataPointSnapshot(registry, name, cacheName);

    assertThat(dataPointSnapshot.getValue()).isEqualTo(value);
  }

  private void assertGaugeMetric(
      PrometheusRegistry registry, String name, String cacheName, double value) {
    final GaugeSnapshot.GaugeDataPointSnapshot dataPointSnapshot =
        (GaugeSnapshot.GaugeDataPointSnapshot) getDataPointSnapshot(registry, name, cacheName);

    assertThat(dataPointSnapshot.getValue()).isEqualTo(value);
  }

  private DataPointSnapshot getDataPointSnapshot(
      PrometheusRegistry registry, String name, String cacheName) {
    final Labels labels = Labels.of(new String[] {"cache"}, new String[] {cacheName});

    return registry.scrape(name::equals).stream()
        .flatMap(metricSnapshot -> metricSnapshot.getDataPoints().stream())
        .filter(dataPoint -> dataPoint.getLabels().equals(labels))
        .findFirst()
        .get();
  }

  private String convertToOpenMetricsFormat(PrometheusRegistry registry) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
    try {
      writer.write(out, registry.scrape());
      return out.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

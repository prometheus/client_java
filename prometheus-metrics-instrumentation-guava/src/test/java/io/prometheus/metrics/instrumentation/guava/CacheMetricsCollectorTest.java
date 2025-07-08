package io.prometheus.metrics.instrumentation.guava;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.nameEscapingScheme;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CacheMetricsCollectorTest {

  @Test
  public void cacheExposesMetricsForHitMissAndEviction() {
    final Cache<String, String> cache =
        CacheBuilder.newBuilder().maximumSize(2).recordStats().build();

    final CacheMetricsCollector collector = new CacheMetricsCollector();
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

    assertCounterMetric(registry, "guava_cache_hit", "users", 1.0);
    assertCounterMetric(registry, "guava_cache_miss", "users", 2.0);
    assertCounterMetric(registry, "guava_cache_requests", "users", 3.0);
    assertCounterMetric(registry, "guava_cache_eviction", "users", 2.0);

    final String expected =
        """
        # TYPE guava_cache_eviction counter
        # HELP guava_cache_eviction Cache eviction totals, doesn't include manually removed entries
        guava_cache_eviction_total{cache="users"} 2.0
        # TYPE guava_cache_hit counter
        # HELP guava_cache_hit Cache hit totals
        guava_cache_hit_total{cache="users"} 1.0
        # TYPE guava_cache_miss counter
        # HELP guava_cache_miss Cache miss totals
        guava_cache_miss_total{cache="users"} 2.0
        # TYPE guava_cache_requests counter
        # HELP guava_cache_requests Cache request totals
        guava_cache_requests_total{cache="users"} 3.0
        # TYPE guava_cache_size gauge
        # HELP guava_cache_size Cache size
        guava_cache_size{cache="users"} 2.0
        # EOF
        """;

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

    final LoadingCache<String, String> cache =
        CacheBuilder.newBuilder().recordStats().build(loader);
    final CacheMetricsCollector collector = new CacheMetricsCollector();
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

    assertCounterMetric(registry, "guava_cache_hit", "loadingusers", 1.0);
    assertCounterMetric(registry, "guava_cache_miss", "loadingusers", 3.0);

    assertCounterMetric(registry, "guava_cache_load_failure", "loadingusers", 1.0);
    assertCounterMetric(registry, "guava_cache_loads", "loadingusers", 3.0);

    final SummarySnapshot.SummaryDataPointSnapshot loadDuration =
        (SummarySnapshot.SummaryDataPointSnapshot)
            getDataPointSnapshot(registry, "guava_cache_load_duration_seconds", "loadingusers");

    assertThat(loadDuration.getCount()).isEqualTo(3);
    assertThat(loadDuration.getSum()).isGreaterThan(0);
  }

  @Test
  public void getPrometheusNamesHasSameSizeAsMetricSizeWhenScraping() {
    final CacheMetricsCollector collector = new CacheMetricsCollector();

    final PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(collector);

    final MetricSnapshots metricSnapshots = registry.scrape();
    final List<String> prometheusNames = collector.getPrometheusNames();

    assertThat(prometheusNames).hasSize(metricSnapshots.size());
  }

  @Test
  public void collectedMetricNamesAreKnownPrometheusNames() {
    final CacheMetricsCollector collector = new CacheMetricsCollector();

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
      nameEscapingScheme = EscapingScheme.NO_ESCAPING;
      writer.write(out, registry.scrape());
      return out.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

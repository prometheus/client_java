package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class PrometheusRegistry {

  public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

  private final List<Collector> collectors = new CopyOnWriteArrayList<>();
  private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<>();
  private final ConcurrentHashMap<String, MetricIdentifier> registeredMetrics =
      new ConcurrentHashMap<>();

  public void register(Collector collector) {
    validateTypeConsistency(collector);
    collectors.add(collector);
    cacheMetricIdentifier(collector);
  }

  public void register(MultiCollector collector) {
    validateTypeConsistency(collector);
    multiCollectors.add(collector);
  }

  /**
   * Validates that the new collector's type is consistent with any existing collectors that have
   * the same Prometheus name. This prevents registering, for example, a Counter and a Gauge with
   * the same name.
   *
   * <p>Uses O(1) lookup via cached metric identifiers for efficiency.
   */
  private void validateTypeConsistency(Collector newCollector) {
    String newName = newCollector.getPrometheusName();
    MetricType newType = newCollector.getMetricType();

    // If name or type is null, skip validation
    if (newName == null || newType == null) {
      return;
    }

    MetricIdentifier newIdentifier = new MetricIdentifier(newName, newType);

    // O(1) lookup in cache
    MetricIdentifier existing = registeredMetrics.get(newName);
    if (existing != null && !newIdentifier.isCompatibleWith(existing)) {
      throw new IllegalArgumentException(
          "Collector with Prometheus name '"
              + newName
              + "' is already registered with type "
              + existing.getType()
              + ", but you are trying to register a new collector with type "
              + newType
              + ". All collectors with the same Prometheus name must have the same type.");
    }
  }

  /**
   * Validates type consistency for MultiCollector.
   *
   * <p>Validates each Prometheus name returned by the MultiCollector. If the MultiCollector
   * provides type information via {@link MultiCollector#getMetricType(String)}, validation happens
   * at registration time. Otherwise, validation is deferred to scrape time.
   */
  private void validateTypeConsistency(MultiCollector newCollector) {
    List<String> names = newCollector.getPrometheusNames();

    for (String name : names) {
      MetricType type = newCollector.getMetricType(name);

      // Skip validation if type is unknown
      if (type == null) {
        continue;
      }

      MetricIdentifier newIdentifier = new MetricIdentifier(name, type);

      // O(1) lookup in cache
      MetricIdentifier existing = registeredMetrics.get(name);
      if (existing != null && !newIdentifier.isCompatibleWith(existing)) {
        throw new IllegalArgumentException(
            "MultiCollector contains a metric with Prometheus name '"
                + name
                + "' and type "
                + type
                + ", but a collector with the same name and type "
                + existing.getType()
                + " is already registered. All collectors with the same Prometheus name must have"
                + " the same type.");
      }

      // Cache the identifier for future lookups
      registeredMetrics.putIfAbsent(name, newIdentifier);
    }
  }

  /**
   * Caches the metric identifier for fast O(1) lookup during future registrations.
   *
   * <p>Only caches if the collector provides both a Prometheus name and type.
   */
  private void cacheMetricIdentifier(Collector collector) {
    String name = collector.getPrometheusName();
    MetricType type = collector.getMetricType();

    if (name != null && type != null) {
      registeredMetrics.putIfAbsent(name, new MetricIdentifier(name, type));
    }
  }

  public void unregister(Collector collector) {
    collectors.remove(collector);
    // Note: We don't remove from cache because another collector with the same name might exist
    // The cache will be cleaned up when clear() is called or can be left as-is (it's just
    // metadata)
  }

  public void unregister(MultiCollector collector) {
    multiCollectors.remove(collector);
  }

  public void clear() {
    collectors.clear();
    multiCollectors.clear();
    registeredMetrics.clear();
  }

  public MetricSnapshots scrape() {
    return scrape((PrometheusScrapeRequest) null);
  }

  public MetricSnapshots scrape(@Nullable PrometheusScrapeRequest scrapeRequest) {
    MetricSnapshots.Builder result = MetricSnapshots.builder();
    for (Collector collector : collectors) {
      MetricSnapshot snapshot =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      if (snapshot != null) {
        result.metricSnapshot(snapshot);
      }
    }
    for (MultiCollector collector : multiCollectors) {
      MetricSnapshots snapshots =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      for (MetricSnapshot snapshot : snapshots) {
        result.metricSnapshot(snapshot);
      }
    }
    return result.build();
  }

  public MetricSnapshots scrape(Predicate<String> includedNames) {
    if (includedNames == null) {
      return scrape();
    }
    return scrape(includedNames, null);
  }

  public MetricSnapshots scrape(
      Predicate<String> includedNames, @Nullable PrometheusScrapeRequest scrapeRequest) {
    if (includedNames == null) {
      return scrape(scrapeRequest);
    }
    MetricSnapshots.Builder result = MetricSnapshots.builder();
    for (Collector collector : collectors) {
      String prometheusName = collector.getPrometheusName();
      // prometheusName == null means the name is unknown, and we have to scrape to learn the name.
      // prometheusName != null means we can skip the scrape if the name is excluded.
      if (prometheusName == null || includedNames.test(prometheusName)) {
        MetricSnapshot snapshot =
            scrapeRequest == null
                ? collector.collect(includedNames)
                : collector.collect(includedNames, scrapeRequest);
        if (snapshot != null) {
          result.metricSnapshot(snapshot);
        }
      }
    }
    for (MultiCollector collector : multiCollectors) {
      List<String> prometheusNames = collector.getPrometheusNames();
      // empty prometheusNames means the names are unknown, and we have to scrape to learn the
      // names.
      // non-empty prometheusNames means we can exclude the collector if all names are excluded by
      // the filter.
      boolean excluded = !prometheusNames.isEmpty();
      for (String prometheusName : prometheusNames) {
        if (includedNames.test(prometheusName)) {
          excluded = false;
          break;
        }
      }
      if (!excluded) {
        MetricSnapshots snapshots =
            scrapeRequest == null
                ? collector.collect(includedNames)
                : collector.collect(includedNames, scrapeRequest);
        for (MetricSnapshot snapshot : snapshots) {
          if (snapshot != null) {
            result.metricSnapshot(snapshot);
          }
        }
      }
    }
    return result.build();
  }
}

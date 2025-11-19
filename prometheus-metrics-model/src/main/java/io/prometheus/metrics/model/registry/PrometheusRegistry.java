package io.prometheus.metrics.model.registry;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class PrometheusRegistry {

  public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

  private final Set<String> prometheusNames = ConcurrentHashMap.newKeySet();
  private final List<Collector> collectors = new CopyOnWriteArrayList<>();
  private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<>();
  private final boolean allowDuplicateRegistration;

  public PrometheusRegistry() {
    this(false);
  }

  /**
   * Create a new PrometheusRegistry.
   *
   * @param allowDuplicateRegistration if true, allows registering multiple collectors with the same
   *     metric name but potentially different label sets. Default is false for backward
   *     compatibility. When enabled, metrics with the same name but different labels can coexist.
   */
  public PrometheusRegistry(boolean allowDuplicateRegistration) {
    this.allowDuplicateRegistration = allowDuplicateRegistration;
  }

  public void register(Collector collector) {
    String prometheusName = collector.getPrometheusName();
    if (prometheusName != null && !allowDuplicateRegistration) {
      if (!prometheusNames.add(prometheusName)) {
        throw new IllegalStateException(
            "Can't register "
                + prometheusName
                + " because a metric with that name is already registered.");
      }
    }
    collectors.add(collector);
  }

  public void register(MultiCollector collector) {
    if (!allowDuplicateRegistration) {
      for (String prometheusName : collector.getPrometheusNames()) {
        if (!prometheusNames.add(prometheusName)) {
          throw new IllegalStateException(
              "Can't register " + prometheusName + " because that name is already registered.");
        }
      }
    }
    multiCollectors.add(collector);
  }

  public void unregister(Collector collector) {
    collectors.remove(collector);
    String prometheusName = collector.getPrometheusName();
    if (prometheusName != null) {
      prometheusNames.remove(collector.getPrometheusName());
    }
  }

  public void unregister(MultiCollector collector) {
    multiCollectors.remove(collector);
    for (String prometheusName : collector.getPrometheusNames()) {
      prometheusNames.remove(prometheusName(prometheusName));
    }
  }

  public void clear() {
    collectors.clear();
    multiCollectors.clear();
    prometheusNames.clear();
  }

  public MetricSnapshots scrape() {
    return scrape((PrometheusScrapeRequest) null);
  }

  public MetricSnapshots scrape(@Nullable PrometheusScrapeRequest scrapeRequest) {
    MetricSnapshots.Builder result =
        MetricSnapshots.builder().allowDuplicates(allowDuplicateRegistration);
    for (Collector collector : collectors) {
      MetricSnapshot snapshot =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      if (snapshot != null) {
        if (!allowDuplicateRegistration
            && result.containsMetricName(snapshot.getMetadata().getName())) {
          throw new IllegalStateException(
              snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
        }
        result.metricSnapshot(snapshot);
      }
    }
    for (MultiCollector collector : multiCollectors) {
      MetricSnapshots snapshots =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      for (MetricSnapshot snapshot : snapshots) {
        if (!allowDuplicateRegistration
            && result.containsMetricName(snapshot.getMetadata().getName())) {
          throw new IllegalStateException(
              snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
        }
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
    MetricSnapshots.Builder result =
        MetricSnapshots.builder().allowDuplicates(allowDuplicateRegistration);
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

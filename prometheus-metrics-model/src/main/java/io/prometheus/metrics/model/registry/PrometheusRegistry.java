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
  private final ConcurrentHashMap<String, RegistrationInfo> registered = new ConcurrentHashMap<>();

  /**
   * Tracks registration information for each metric name to enable validation of type consistency
   * and label schema uniqueness.
   */
  private static class RegistrationInfo {
    private final MetricType type;
    private final Set<Set<String>> labelSets;

    RegistrationInfo(MetricType type, @Nullable Set<String> labelNames) {
      this.type = type;
      this.labelSets = ConcurrentHashMap.newKeySet();
      if (labelNames != null) {
        this.labelSets.add(labelNames);
      }
    }

    /**
     * Adds a new label schema to this registration.
     *
     * @param labelNames the label names to add
     * @return true if the label schema was added, false if it already exists
     */
    boolean addLabelSet(@Nullable Set<String> labelNames) {
      if (labelNames == null) {
        return true;
      }
      return labelSets.add(labelNames);
    }

    MetricType getType() {
      return type;
    }
  }

  public void register(Collector collector) {
    if (collectors.contains(collector)) {
      return;
    }

    String prometheusName = collector.getPrometheusName();
    MetricType metricType = collector.getMetricType();
    Set<String> labelNames = collector.getLabelNames();

    if (prometheusName != null && metricType != null) {
      registered.compute(
          prometheusName,
          (name, existingInfo) -> {
            if (existingInfo == null) {
              return new RegistrationInfo(metricType, labelNames);
            } else {
              if (existingInfo.getType() != metricType) {
                throw new IllegalArgumentException(
                    prometheusName
                        + ": Conflicting metric types. Existing: "
                        + existingInfo.getType()
                        + ", new: "
                        + metricType);
              }

              // Check label schema uniqueness (if label names provided)
              if (labelNames != null) {
                if (!existingInfo.addLabelSet(labelNames)) {
                  throw new IllegalArgumentException(
                      prometheusName
                          + ": Duplicate label schema. A metric with the same name, type, and label"
                          + " names is already registered.");
                }
              }

              return existingInfo;
            }
          });
    }

    if (prometheusName != null) {
      prometheusNames.add(prometheusName);
    }

    collectors.add(collector);
  }

  public void register(MultiCollector collector) {
    if (multiCollectors.contains(collector)) {
      return;
    }

    List<String> names = collector.getPrometheusNames();

    for (String prometheusName : names) {
      MetricType metricType = collector.getMetricType(prometheusName);
      Set<String> labelNames = collector.getLabelNames(prometheusName);

      if (metricType != null) {
        registered.compute(
            prometheusName,
            (name, existingInfo) -> {
              if (existingInfo == null) {
                return new RegistrationInfo(metricType, labelNames);
              } else {
                if (existingInfo.getType() != metricType) {
                  throw new IllegalArgumentException(
                      prometheusName
                          + ": Conflicting metric types. Existing: "
                          + existingInfo.getType()
                          + ", new: "
                          + metricType);
                }

                if (labelNames != null) {
                  if (!existingInfo.addLabelSet(labelNames)) {
                    throw new IllegalArgumentException(
                        prometheusName
                            + ": Duplicate label schema. A metric with the same name, type, and"
                            + " label names is already registered.");
                  }
                }

                return existingInfo;
              }
            });
      }

      prometheusNames.add(prometheusName);
    }

    multiCollectors.add(collector);
  }

  public void unregister(Collector collector) {
    collectors.remove(collector);
    String prometheusName = collector.getPrometheusName();
    if (prometheusName != null) {
      // Check if any other collectors are still using this name
      nameInUse(prometheusName);
    }
  }

  public void unregister(MultiCollector collector) {
    multiCollectors.remove(collector);
    for (String prometheusName : collector.getPrometheusNames()) {
      // Check if any other collectors are still using this name
      nameInUse(prometheusName);
    }
  }

  private void nameInUse(String prometheusName) {
    boolean nameStillInUse = false;
    for (Collector c : collectors) {
      if (prometheusName.equals(c.getPrometheusName())) {
        nameStillInUse = true;
        break;
      }
    }
    if (!nameStillInUse) {
      for (MultiCollector mc : multiCollectors) {
        if (mc.getPrometheusNames().contains(prometheusName)) {
          nameStillInUse = true;
          break;
        }
      }
    }
    if (!nameStillInUse) {
      prometheusNames.remove(prometheusName);
      // Also remove from registered since no collectors use this name anymore
      registered.remove(prometheusName);
    }
  }

  public void clear() {
    collectors.clear();
    multiCollectors.clear();
    prometheusNames.clear();
    registered.clear();
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
        if (result.containsMetricName(snapshot.getMetadata().getName())) {
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
        if (result.containsMetricName(snapshot.getMetadata().getName())) {
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

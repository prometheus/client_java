package io.prometheus.metrics.model.registry;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    List<MetricSnapshot> allSnapshots = new ArrayList<>();
    for (Collector collector : collectors) {
      MetricSnapshot snapshot =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      if (snapshot != null) {
        allSnapshots.add(snapshot);
      }
    }
    for (MultiCollector collector : multiCollectors) {
      MetricSnapshots snapshots =
          scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
      for (MetricSnapshot snapshot : snapshots) {
        allSnapshots.add(snapshot);
      }
    }

    // Validate no duplicate label schemas for same metric name
    validateNoDuplicateLabelSchemas(allSnapshots);

    MetricSnapshots.Builder result = MetricSnapshots.builder();
    for (MetricSnapshot snapshot : allSnapshots) {
      result.metricSnapshot(snapshot);
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

  /**
   * Validates that snapshots with the same metric name don't have identical label schemas. This
   * prevents duplicate time series which would occur if two snapshots produce data points with
   * identical label sets.
   */
  private void validateNoDuplicateLabelSchemas(List<MetricSnapshot> snapshots) {
    // Group snapshots by prometheus name
    Map<String, List<MetricSnapshot>> snapshotsByName = new HashMap<>();
    for (MetricSnapshot snapshot : snapshots) {
      String name = snapshot.getMetadata().getPrometheusName();
      snapshotsByName.computeIfAbsent(name, k -> new ArrayList<>()).add(snapshot);
    }

    // For each group with the same name, check for duplicate label schemas
    for (Map.Entry<String, List<MetricSnapshot>> entry : snapshotsByName.entrySet()) {
      List<MetricSnapshot> group = entry.getValue();
      if (group.size() <= 1) {
        continue; // No duplicates possible with only one snapshot
      }

      // Extract label schemas from each snapshot
      List<Set<String>> labelSchemas = new ArrayList<>();
      for (MetricSnapshot snapshot : group) {
        Set<String> labelSchema = extractLabelSchema(snapshot);
        if (labelSchema != null) {
          // Check if this label schema already exists
          if (labelSchemas.contains(labelSchema)) {
            throw new IllegalStateException(
                snapshot.getMetadata().getPrometheusName()
                    + ": duplicate metric name with identical label schema "
                    + labelSchema);
          }
          labelSchemas.add(labelSchema);
        }
      }
    }
  }

  /**
   * Extracts the label schema (set of label names) from a snapshot's data points. Returns null if
   * the snapshot has no data points or if data points have inconsistent label schemas.
   */
  private Set<String> extractLabelSchema(MetricSnapshot snapshot) {
    if (snapshot.getDataPoints().isEmpty()) {
      return null;
    }

    // Get label names from the first data point
    DataPointSnapshot firstDataPoint = snapshot.getDataPoints().get(0);
    Set<String> labelNames = new HashSet<>();
    for (int i = 0; i < firstDataPoint.getLabels().size(); i++) {
      labelNames.add(firstDataPoint.getLabels().getName(i));
    }

    // Verify all data points have the same label schema
    for (DataPointSnapshot dataPoint : snapshot.getDataPoints()) {
      Set<String> currentLabelNames = new HashSet<>();
      for (int i = 0; i < dataPoint.getLabels().size(); i++) {
        currentLabelNames.add(dataPoint.getLabels().getName(i));
      }
      if (!currentLabelNames.equals(labelNames)) {
        // Data points have inconsistent label schemas - this is unusual but valid
        // We can't determine a single label schema, so return null
        return null;
      }
    }

    return labelNames;
  }
}

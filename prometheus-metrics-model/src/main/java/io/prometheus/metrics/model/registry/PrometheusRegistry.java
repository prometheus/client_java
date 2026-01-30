package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class PrometheusRegistry {

  public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

  private final Set<String> prometheusNames = ConcurrentHashMap.newKeySet();
  private final Set<Collector> collectors = ConcurrentHashMap.newKeySet();
  private final Set<MultiCollector> multiCollectors = ConcurrentHashMap.newKeySet();
  private final ConcurrentHashMap<String, RegistrationInfo> registered = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Collector, CollectorRegistration> collectorMetadata =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<MultiCollector, List<MultiCollectorRegistration>>
      multiCollectorMetadata = new ConcurrentHashMap<>();

  /** Stores the registration details for a Collector at registration time. */
  private static class CollectorRegistration {
    final String prometheusName;
    final Set<String> labelNames;

    CollectorRegistration(String prometheusName, @Nullable Set<String> labelNames) {
      this.prometheusName = prometheusName;
      this.labelNames =
          (labelNames == null || labelNames.isEmpty()) ? Collections.emptySet() : labelNames;
    }
  }

  /**
   * Stores the registration details for a single metric within a MultiCollector. A MultiCollector
   * can produce multiple metrics, so we need one of these per metric name.
   */
  private static class MultiCollectorRegistration {
    final String prometheusName;
    final Set<String> labelNames;

    MultiCollectorRegistration(String prometheusName, @Nullable Set<String> labelNames) {
      this.prometheusName = prometheusName;
      this.labelNames =
          (labelNames == null || labelNames.isEmpty()) ? Collections.emptySet() : labelNames;
    }
  }

  /**
   * Tracks registration information for each metric name to enable validation of type consistency,
   * label schema uniqueness, and help/unit consistency. Stores metadata to enable O(1)
   * unregistration without iterating through all collectors.
   */
  private static class RegistrationInfo {
    private final MetricType type;
    private final Set<Set<String>> labelSchemas;
    @Nullable private final String help;
    @Nullable private final Unit unit;

    private RegistrationInfo(
        MetricType type,
        Set<Set<String>> labelSchemas,
        @Nullable String help,
        @Nullable Unit unit) {
      this.type = type;
      this.labelSchemas = labelSchemas;
      this.help = help;
      this.unit = unit;
    }

    static RegistrationInfo of(
        MetricType type,
        @Nullable Set<String> labelNames,
        @Nullable String help,
        @Nullable Unit unit) {
      Set<Set<String>> labelSchemas = ConcurrentHashMap.newKeySet();
      Set<String> normalized =
          (labelNames == null || labelNames.isEmpty()) ? Collections.emptySet() : labelNames;
      labelSchemas.add(normalized);
      return new RegistrationInfo(type, labelSchemas, help, unit);
    }

    /**
     * Validates that the given help and unit are consistent with this registration. Throws if both
     * sides have non-null help/unit and they differ.
     */
    void validateMetadata(@Nullable String newHelp, @Nullable Unit newUnit) {
      if (help != null && newHelp != null && !Objects.equals(help, newHelp)) {
        throw new IllegalArgumentException(
            "Conflicting help strings. Existing: \"" + help + "\", new: \"" + newHelp + "\"");
      }
      if (unit != null && newUnit != null && !Objects.equals(unit, newUnit)) {
        throw new IllegalArgumentException(
            "Conflicting unit. Existing: " + unit + ", new: " + newUnit);
      }
    }

    /**
     * Adds a label schema to this registration.
     *
     * @param labelNames the label names to add (null or empty sets are normalized to empty set)
     * @return true if the schema was added (new), false if it already existed
     */
    boolean addLabelSet(@Nullable Set<String> labelNames) {
      Set<String> normalized =
          (labelNames == null || labelNames.isEmpty()) ? Collections.emptySet() : labelNames;
      return labelSchemas.add(normalized);
    }

    /**
     * Removes a label schema from this registration.
     *
     * @param labelNames the label names to remove (null or empty sets are normalized to empty set)
     */
    void removeLabelSet(@Nullable Set<String> labelNames) {
      Set<String> normalized =
          (labelNames == null || labelNames.isEmpty()) ? Collections.emptySet() : labelNames;
      labelSchemas.remove(normalized);
    }

    /** Returns true if all label schemas have been unregistered. */
    boolean isEmpty() {
      return labelSchemas.isEmpty();
    }

    MetricType getType() {
      return type;
    }
  }

  public void register(Collector collector) {
    if (collectors.contains(collector)) {
      throw new IllegalArgumentException("Collector instance is already registered");
    }

    String prometheusName = collector.getPrometheusName();
    MetricType metricType = collector.getMetricType();
    Set<String> labelNames = collector.getLabelNames();
    MetricMetadata metadata = collector.getMetadata();
    String help = metadata != null ? metadata.getHelp() : null;
    Unit unit = metadata != null ? metadata.getUnit() : null;

    // Only perform validation if collector provides sufficient metadata.
    // Collectors that don't implement getPrometheusName()/getMetricType() will skip validation.
    if (prometheusName != null && metricType != null) {
      final String name = prometheusName;
      final MetricType type = metricType;
      final Set<String> names = labelNames;
      final String helpForValidation = help;
      final Unit unitForValidation = unit;
      registered.compute(
          prometheusName,
          (n, existingInfo) -> {
            if (existingInfo == null) {
              return RegistrationInfo.of(type, names, helpForValidation, unitForValidation);
            } else {
              if (existingInfo.getType() != type) {
                throw new IllegalArgumentException(
                    name
                        + ": Conflicting metric types. Existing: "
                        + existingInfo.getType()
                        + ", new: "
                        + type);
              }
              existingInfo.validateMetadata(helpForValidation, unitForValidation);
              if (!existingInfo.addLabelSet(names)) {
                Set<String> normalized =
                    (names == null || names.isEmpty()) ? Collections.emptySet() : names;
                throw new IllegalArgumentException(
                    name + ": duplicate metric name with identical label schema " + normalized);
              }
              return existingInfo;
            }
          });

      collectorMetadata.put(collector, new CollectorRegistration(prometheusName, labelNames));
    }

    if (prometheusName != null) {
      prometheusNames.add(prometheusName);
    }

    collectors.add(collector);
  }

  public void register(MultiCollector collector) {
    if (multiCollectors.contains(collector)) {
      throw new IllegalArgumentException("MultiCollector instance is already registered");
    }

    List<String> prometheusNamesList = collector.getPrometheusNames();
    List<MultiCollectorRegistration> registrations = new ArrayList<>();

    for (String prometheusName : prometheusNamesList) {
      MetricType metricType = collector.getMetricType(prometheusName);
      Set<String> labelNames = collector.getLabelNames(prometheusName);
      MetricMetadata metadata = collector.getMetadata(prometheusName);
      String help = metadata != null ? metadata.getHelp() : null;
      Unit unit = metadata != null ? metadata.getUnit() : null;

      if (metricType != null) {
        final MetricType type = metricType;
        final Set<String> labelNamesForValidation = labelNames;
        final String helpForValidation = help;
        final Unit unitForValidation = unit;
        registered.compute(
            prometheusName,
            (name, existingInfo) -> {
              if (existingInfo == null) {
                return RegistrationInfo.of(
                    type, labelNamesForValidation, helpForValidation, unitForValidation);
              } else {
                if (existingInfo.getType() != type) {
                  throw new IllegalArgumentException(
                      prometheusName
                          + ": Conflicting metric types. Existing: "
                          + existingInfo.getType()
                          + ", new: "
                          + type);
                }
                existingInfo.validateMetadata(helpForValidation, unitForValidation);
                if (!existingInfo.addLabelSet(labelNamesForValidation)) {
                  Set<String> normalized =
                      (labelNamesForValidation == null || labelNamesForValidation.isEmpty())
                          ? Collections.emptySet()
                          : labelNamesForValidation;
                  throw new IllegalArgumentException(
                      prometheusName
                          + ": duplicate metric name with identical label schema "
                          + normalized);
                }
                return existingInfo;
              }
            });

        registrations.add(new MultiCollectorRegistration(prometheusName, labelNames));
      }

      prometheusNames.add(prometheusName);
    }

    multiCollectorMetadata.put(collector, registrations);
    multiCollectors.add(collector);
  }

  public void unregister(Collector collector) {
    collectors.remove(collector);

    CollectorRegistration registration = collectorMetadata.remove(collector);
    if (registration != null && registration.prometheusName != null) {
      unregisterLabelSchema(registration.prometheusName, registration.labelNames);
    }
  }

  public void unregister(MultiCollector collector) {
    multiCollectors.remove(collector);

    List<MultiCollectorRegistration> registrations = multiCollectorMetadata.remove(collector);
    if (registrations != null) {
      for (MultiCollectorRegistration registration : registrations) {
        unregisterLabelSchema(registration.prometheusName, registration.labelNames);
      }
    }
  }

  /**
   * Decrements the reference count for a label schema and removes the metric name entirely if no
   * schemas remain.
   */
  private void unregisterLabelSchema(String prometheusName, Set<String> labelNames) {
    registered.computeIfPresent(
        prometheusName,
        (name, info) -> {
          info.removeLabelSet(labelNames);
          if (info.isEmpty()) {
            // No more label schemas for this name, remove it entirely
            prometheusNames.remove(prometheusName);
            return null; // remove from registered map
          }
          return info; // keep the RegistrationInfo, just with decremented count
        });
  }

  public void clear() {
    collectors.clear();
    multiCollectors.clear();
    prometheusNames.clear();
    registered.clear();
    collectorMetadata.clear();
    multiCollectorMetadata.clear();
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
    List<MetricSnapshot> allSnapshots = new ArrayList<>();
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
          allSnapshots.add(snapshot);
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
            allSnapshots.add(snapshot);
          }
        }
      }
    }

    MetricSnapshots.Builder result = MetricSnapshots.builder();
    for (MetricSnapshot snapshot : allSnapshots) {
      result.metricSnapshot(snapshot);
    }
    return result.build();
  }
}

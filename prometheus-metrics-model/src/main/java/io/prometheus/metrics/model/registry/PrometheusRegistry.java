package io.prometheus.metrics.model.registry;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
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

  public void register(Collector collector) {
    collectors.add(collector);
  }

  public void register(MultiCollector collector) {
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

  /**
   * Validates that there are no duplicate time series (same metric name + same label set) across
   * all collected snapshots, and that all metrics with the same name have the same type.
   */
  private void validateNoDuplicateTimeSeries(MetricSnapshots snapshots) {
    // Group snapshots by Prometheus name
    Map<String, List<MetricSnapshot>> groupedByName = new HashMap<>();
    for (MetricSnapshot snapshot : snapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      groupedByName.computeIfAbsent(prometheusName, k -> new ArrayList<>()).add(snapshot);
    }

    // For each group with multiple snapshots, validate type consistency and check for duplicate
    // labels
    for (Map.Entry<String, List<MetricSnapshot>> entry : groupedByName.entrySet()) {
      if (entry.getValue().size() > 1) {
        String prometheusName = entry.getKey();
        List<MetricSnapshot> snapshotsWithSameName = entry.getValue();

        // Check that all snapshots with the same name have the same type
        // Q: What if you have a counter named "foo" and a gauge named "foo"?
        // A: This is invalid. While counter produces "foo_total" and gauge produces "foo",
        //    they both use the same name for HELP/TYPE/UNIT metadata, creating a conflict.
        Class<?> firstType = snapshotsWithSameName.get(0).getClass();
        for (int i = 1; i < snapshotsWithSameName.size(); i++) {
          MetricSnapshot snapshot = snapshotsWithSameName.get(i);
          if (!firstType.equals(snapshot.getClass())) {
            throw new IllegalStateException(
                "Conflicting metric types for Prometheus name '"
                    + prometheusName
                    + "': "
                    + firstType.getSimpleName()
                    + " vs "
                    + snapshot.getClass().getSimpleName()
                    + ". All metrics with the same Prometheus name must have the same type.");
          }
        }

        // Check for duplicate label sets
        Set<Labels> seenLabels = new HashSet<>();
        for (MetricSnapshot snapshot : snapshotsWithSameName) {
          for (DataPointSnapshot dataPoint : snapshot.getDataPoints()) {
            Labels labels = dataPoint.getLabels();
            if (!seenLabels.add(labels)) {
              throw new IllegalStateException(
                  "Duplicate labels detected for metric '"
                      + prometheusName
                      + "': "
                      + labels
                      + ". Each time series (metric name + label set) must be unique.");
            }
          }
        }
      }
    }
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
    MetricSnapshots snapshots = result.build();
    validateNoDuplicateTimeSeries(snapshots);
    return snapshots;
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
    MetricSnapshots snapshots = result.build();
    validateNoDuplicateTimeSeries(snapshots);
    return snapshots;
  }
}

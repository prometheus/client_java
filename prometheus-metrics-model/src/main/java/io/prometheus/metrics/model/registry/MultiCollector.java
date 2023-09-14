package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Like {@link Collector}, but collecting multiple Snapshots at once.
 */
@FunctionalInterface
public interface MultiCollector {

    /**
     * Called when the Prometheus server scrapes metrics.
     */
    MetricSnapshots collect();

    /**
     * Like {@link #collect()}, but returns only the snapshots where {@code includedNames.test(name)} is {@code true}.
     * <p>
     * Override this if there is a more efficient way than first collecting all snapshot and then discarding the excluded ones.
     */
    default MetricSnapshots collect(Predicate<String> includedNames) {
        MetricSnapshots allSnapshots = collect();
        MetricSnapshots.Builder result = MetricSnapshots.builder();
        for (MetricSnapshot snapshot : allSnapshots) {
            if (includedNames.test(snapshot.getMetadata().getPrometheusName())) {
                result.metricSnapshot(snapshot);
            }
        }
        return result.build();
    }

    /**
     * Override this and return an empty list if the MultiCollector does not return a constant list of names
     * (names may be added / removed between scrapes).
     */
    default List<String> getPrometheusNames() {
        return collect().stream().map(snapshot -> snapshot.getMetadata().getPrometheusName()).collect(Collectors.toList());
    }
}

package io.prometheus.metrics.model.snapshots;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable list of metric snapshots.
 * Guaranteed entries have unique metric names.
 */
public class MetricSnapshots implements Iterable<MetricSnapshot> {

    private final List<MetricSnapshot> snapshots;

    /**
     * To create MetricSnapshots, use builder that takes care of all validations.
     */
    private MetricSnapshots(Collection<MetricSnapshot> snapshots) {
        this.snapshots = Collections.unmodifiableList(new ArrayList<>(snapshots));
    }

    /**
     * TODO: just for compatibility
     */
    public MetricSnapshots(MetricSnapshot... snapshots) {
        this.snapshots = builder().metricSnapshots(snapshots).build().snapshots;
    }

    public static MetricSnapshots empty() {
        return new MetricSnapshots(Collections.emptyList());
    }

    public static MetricSnapshots of(MetricSnapshot... snapshots) {
        return builder().metricSnapshots(snapshots).build();
    }

    public MetricSnapshots filter(Predicate<String> nameFilter) {
        var result = snapshots
                .stream()
                .filter(snapshot -> snapshot.matches(nameFilter))
                .collect(Collectors.toList());
        return new MetricSnapshots(result);
    }

    public MetricSnapshots withLabels(Labels labels) {
        var result = snapshots
                .stream()
                .map(snapshot -> snapshot.withLabels(labels))
                .collect(Collectors.toList());
        return new MetricSnapshots(result);
    }

    public MetricSnapshots withNamePrefix(String prefix) {
        var result = snapshots
                .stream()
                .map(snapshot -> snapshot.withNamePrefix(prefix))
                .collect(Collectors.toList());
        return new MetricSnapshots(result);
    }


    @Override
    public Iterator<MetricSnapshot> iterator() {
        return snapshots.iterator();
    }

    public int size() {
        return snapshots.size();
    }

    public MetricSnapshot get(int i) {
        return snapshots.get(i);
    }

    public Stream<MetricSnapshot> stream() {
        return snapshots.stream();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        /** Used to merge metrics by prometheus names, and to produce correct output order. */
        private final Map<String, MetricSnapshot> result = new TreeMap<>();

        private Builder() {
        }

        /**
         * Add a metric snapshot. Call multiple times to add multiple metric snapshots.
         */
        public Builder metricSnapshot(MetricSnapshot snapshot) {
            result.merge(snapshot.getMetadata().getPrometheusName(), snapshot, MetricSnapshot::merge);
            return this;
        }

        /**
         * Add a metric snapshot collection.
         */
        public Builder metricSnapshots(Iterable<MetricSnapshot> snapshots) {
            snapshots.forEach(this::metricSnapshot);
            return this;
        }

        /**
         * Add a metric snapshot collection.
         */
        public Builder metricSnapshots(MetricSnapshot[] snapshots) {
            for (MetricSnapshot snapshot : snapshots) this.metricSnapshot(snapshot);
            return this;
        }

        public MetricSnapshots build() {
            return new MetricSnapshots(result.values());
        }
    }
}

package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

/**
 * Immutable list of metric snapshots.
 */
public class MetricSnapshots implements Iterable<MetricSnapshot> {

    private final List<MetricSnapshot> snapshots;

    /**
     * See {@link #MetricSnapshots(Collection)}
     */
    public MetricSnapshots(MetricSnapshot... snapshots) {
        this(Arrays.asList(snapshots));
    }

    /**
     * To create MetricSnapshots, you can either call the constructor directly
     * or use {@link #builder()}.
     *
     * @param snapshots the constructor creates a sorted copy of snapshots.
     * @throws IllegalArgumentException if snapshots contains duplicate metric names.
     *                                  To avoid duplicate metric names use {@link #builder()} and check
     *                                  {@link Builder#containsMetricName(String)} before calling
     *                                  {@link Builder#metricSnapshot(MetricSnapshot)}.
     */
    public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
        List<MetricSnapshot> list = new ArrayList<>(snapshots);
        list.sort(comparing(s -> s.getMetadata().getPrometheusName()));
        for (int i = 0; i < snapshots.size() - 1; i++) {
            if (list.get(i).getMetadata().getPrometheusName().equals(list.get(i + 1).getMetadata().getPrometheusName())) {
                throw new IllegalArgumentException(list.get(i).getMetadata().getPrometheusName() + ": duplicate metric name");
            }
        }
        this.snapshots = unmodifiableList(list);
    }

    public static MetricSnapshots of(MetricSnapshot... snapshots) {
        return new MetricSnapshots(snapshots);
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

        private final List<MetricSnapshot> snapshots = new ArrayList<>();

        private Builder() {
        }

        public boolean containsMetricName(String name) {
            if (name == null) {
                return false;
            }
            String prometheusName = prometheusName(name);
            for (MetricSnapshot snapshot : snapshots) {
                if (snapshot.getMetadata().getPrometheusName().equals(prometheusName)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Add a metric snapshot. Call multiple times to add multiple metric snapshots.
         */
        public Builder metricSnapshot(MetricSnapshot snapshot) {
            snapshots.add(snapshot);
            return this;
        }

        public MetricSnapshots build() {
            return new MetricSnapshots(snapshots);
        }
    }
}

package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

public class MetricSnapshots implements Iterable<MetricSnapshot> {

    private final List<MetricSnapshot> snapshots;

    public MetricSnapshots(MetricSnapshot... snapshots) {
        // TODO: Validate name conflicts in snapshots
        this(Arrays.asList(snapshots));
    }

    public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
        List<MetricSnapshot> list = new ArrayList<>(snapshots);
        list.sort(comparing(s -> s.getMetadata().getName()));
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

    public Stream<MetricSnapshot> stream() {
        return snapshots.stream();
    }
}

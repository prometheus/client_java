package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public class Labels implements Comparable<Labels>, Iterable<Label> {

    public static final Labels EMPTY = new Labels(new String[]{}, new String[]{});

    private final String[] names; // sorted
    private final String[] values;

    private Labels(String[] names, String[] values) {
        this.names = names;
        this.values = values;
    }

    public static Labels of(String... keyValuePairs) {
        return null;
    }
    public static Labels of(String[] names, String[] values) {
        String[] namesCopy = Arrays.copyOf(names, names.length);
        String[] valuesCopy = Arrays.copyOf(values, values.length);
        sortAndValidate(namesCopy, valuesCopy);
        return new Labels(namesCopy, valuesCopy);
    }

    private static void sortAndValidate(String[] names, String[] values) {
        // names.length == values.length
        // implement regex for names here?
        // make sure there are no duplicates in names
        // sort arrays by name
    }

    public Labels merge(String[] names, String[] values) {
        if (this.equals(EMPTY)) {
            return Labels.of(names, values);
        }
        String[] mergedNames = new String[this.names.length + names.length];
        String[] mergedValues = new String[this.values.length + values.length];
        System.arraycopy(this.names, 0, mergedNames, 0, this.names.length);
        System.arraycopy(this.values, 0, mergedValues, 0, this.values.length);
        System.arraycopy(names, 0, mergedNames, this.names.length, names.length);
        System.arraycopy(values, 0, mergedValues, this.values.length, values.length);
        sortAndValidate(mergedNames, mergedValues);
        return new Labels(mergedNames, mergedValues);
    }

    @Override
    public int compareTo(Labels o) {
        return 0;
    }

    @Override
    public Iterator<Label> iterator() {
        // TODO, this is just a quick hack
        List<Label> result = new ArrayList<>(names.length);
        for (int i=0; i<names.length; i++) {
            result.add(new Label(names[i], values[i]));
        }
        return result.iterator();
    }

    // as labels are sorted by name, equals is insensitive to the order
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Labels labels = (Labels) o;
        return Arrays.equals(names, labels.names) && Arrays.equals(values, labels.values);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(names);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}

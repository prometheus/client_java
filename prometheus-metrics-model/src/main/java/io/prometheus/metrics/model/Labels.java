package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public class Labels implements Comparable<Labels>, Iterable<Label> {

    public static final Labels EMPTY = new Labels(new String[]{}, new String[]{});
    private static final Pattern LABEL_NAME_RE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final String[] names; // sorted
    private final String[] values;

    private Labels(String[] names, String[] values) {
        // names and values are already sorted and validated
        this.names = names;
        this.values = values;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.equals(EMPTY);
    }

    public static Labels of(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value pairs must have an even length");
        }
        String[] names = new String[keyValuePairs.length / 2];
        String[] values = new String[keyValuePairs.length / 2];
        for (int i = 0; 2 * i < keyValuePairs.length; i++) {
            names[i] = keyValuePairs[2 * i];
            values[i] = keyValuePairs[2 * i + 1];
        }
        return Labels.of(names, values);
    }

    public static Labels of(String[] names, String[] values) {
        String[] namesCopy = Arrays.copyOf(names, names.length);
        String[] valuesCopy = Arrays.copyOf(values, values.length);
        sortAndValidate(namesCopy, valuesCopy);
        return new Labels(namesCopy, valuesCopy);
    }

    public boolean contains(String labelName) {
        for (String name : names) {
            if (name.equals(labelName)) {
                return true;
            }
        }
        return false;
    }

    private static void sortAndValidate(String[] names, String[] values) {
        // names.length == values.length
        // implement regex for names here?
        // make sure there are no duplicates in names
        // sort arrays by name
        validateNames(names);
        sort(names, values);
    }

    private static void validateNames(String[] names) {
        // TODO: Duplicate names are illegal
        for (String name : names) {
            if (!isValidLabelName(name)) {
                throw new IllegalArgumentException("'" + name + "' is an illegal label name");
            }
        }
    }

    private static void sort(String[] names, String[] values) {
        // bubblesort :)
        int n = names.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (names[j].compareTo(names[j + 1]) > 0) {
                    swap(j, j+1, names, values);
                }
            }
        }
    }

    private static void swap(int i, int j, String[] names, String[] values) {
        String tmp = names[j];
        names[j] = names[i];
        names[i] = tmp;
        tmp = values[j];
        values[j] = values[i];
        values[i] = tmp;
    }

    public static boolean isValidLabelName(String name) {
        return LABEL_NAME_RE.matcher(name).matches() && !name.startsWith("__");
    }

    public Labels merge(Labels other) {
        String[] names = new String[this.names.length + other.names.length];
        String[] values = new String[names.length];
        int thisPos = 0;
        int otherPos = 0;
        while (thisPos + otherPos < names.length) {
            if (thisPos >= this.names.length) {
                names[thisPos + otherPos] = other.names[otherPos];
                values[thisPos + otherPos] = other.values[otherPos];
                otherPos++;
            } else if (otherPos >= other.names.length) {
                names[thisPos + otherPos] = this.names[thisPos];
                values[thisPos + otherPos] = this.values[thisPos];
                thisPos++;
            } else if (this.names[thisPos].compareTo(other.names[otherPos]) < 0) {
                names[thisPos + otherPos] = this.names[thisPos];
                values[thisPos + otherPos] = this.values[thisPos];
                thisPos++;
            } else if (this.names[thisPos].compareTo(other.names[otherPos]) > 0) {
                names[thisPos + otherPos] = other.names[otherPos];
                values[thisPos + otherPos] = other.values[otherPos];
                otherPos++;
            } else {
                throw new IllegalArgumentException("duplicate label name " + this.names[thisPos]);
            }
        }
        return new Labels(names, values);
    }

    public Labels add(String name, String value) {
        return merge(Labels.of(name, value));
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
        for (int i = 0; i < names.length; i++) {
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

package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

/**
 * Immutable set of name/value pairs, sorted by name.
 */
public class Labels implements Comparable<Labels>, Iterable<Label> {

    public static final Labels EMPTY;

    static {
        String[] names = new String[]{};
        String[] values = new String[]{};
        EMPTY = new Labels(names, names, values);
    }

    // prometheusNames is the same as names, but dots are replaced with underscores.
    // Labels is sorted by prometheusNames.
    // If names[i] does not contain a dot, prometheusNames[i] references the same String as names[i]
    // so that we don't have unnecessary duplicates of strings.
    // If none of the names contains a dot, then prometheusNames references the same array as names
    // so that we don't have unnecessary duplicate arrays.
    private final String[] prometheusNames;
    private final String[] names;
    private final String[] values;

    private Labels(String[] names, String[] prometheusNames, String[] values) {
        this.names = names;
        this.prometheusNames = prometheusNames;
        this.values = values;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.equals(EMPTY);
    }

    /**
     * Create a new Labels instance.
     * You can either create Labels with one of the static {@code Labels.of(...)} methods,
     * or you can use the {@link Labels#builder()}.
     *
     * @param keyValuePairs as in {@code {name1, value1, name2, value2}}. Length must be even.
     *                      {@link PrometheusNaming#isValidLabelName(String)} must be true for each name.
     *                      Use {@link PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary strings
     *                      to valid label names. Label names must be unique (no duplicate label names).
     */
    public static Labels of(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value pairs must have an even length");
        }
        if (keyValuePairs.length == 0) {
            return EMPTY;
        }
        String[] names = new String[keyValuePairs.length / 2];
        String[] values = new String[keyValuePairs.length / 2];
        for (int i = 0; 2 * i < keyValuePairs.length; i++) {
            names[i] = keyValuePairs[2 * i];
            values[i] = keyValuePairs[2 * i + 1];
        }
        String[] prometheusNames = makePrometheusNames(names);
        sortAndValidate(names, prometheusNames, values);
        return new Labels(names, prometheusNames, values);
    }

    // package private for testing
    static String[] makePrometheusNames(String[] names) {
        String[] prometheusNames = names;
        for (int i=0; i<names.length; i++) {
            if (names[i].contains(".")) {
                if (prometheusNames == names) {
                    prometheusNames = Arrays.copyOf(names, names.length);
                }
                prometheusNames[i] = PrometheusNaming.prometheusName(names[i]);
            }
        }
        return prometheusNames;
    }

    /**
     * Create a new Labels instance.
     * You can either create Labels with one of the static {@code Labels.of(...)} methods,
     * or you can use the {@link Labels#builder()}.
     *
     * @param names  label names. {@link PrometheusNaming#isValidLabelName(String)} must be true for each name.
     *               Use {@link PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary strings
     *               to valid label names. Label names must be unique (no duplicate label names).
     * @param values label values. {@code names.size()} must be equal to {@code values.size()}.
     */
    public static Labels of(List<String> names, List<String> values) {
        if (names.size() != values.size()) {
            throw new IllegalArgumentException("Names and values must have the same size.");
        }
        if (names.size() == 0) {
            return EMPTY;
        }
        String[] namesCopy = names.toArray(new String[0]);
        String[] valuesCopy = values.toArray(new String[0]);
        String[] prometheusNames = makePrometheusNames(namesCopy);
        sortAndValidate(namesCopy, prometheusNames, valuesCopy);
        return new Labels(namesCopy, prometheusNames, valuesCopy);
    }

    /**
     * Create a new Labels instance.
     * You can either create Labels with one of the static {@code Labels.of(...)} methods,
     * or you can use the {@link Labels#builder()}.
     *
     * @param names  label names. {@link PrometheusNaming#isValidLabelName(String)} must be true for each name.
     *               Use {@link PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary strings
     *               to valid label names. Label names must be unique (no duplicate label names).
     * @param values label values. {@code names.length} must be equal to {@code values.length}.
     */
    public static Labels of(String[] names, String[] values) {
        if (names.length != values.length) {
            throw new IllegalArgumentException("Names and values must have the same length.");
        }
        if (names.length == 0) {
            return EMPTY;
        }
        String[] namesCopy = Arrays.copyOf(names, names.length);
        String[] valuesCopy = Arrays.copyOf(values, values.length);
        String[] prometheusNames = makePrometheusNames(namesCopy);
        sortAndValidate(namesCopy, prometheusNames, valuesCopy);
        return new Labels(namesCopy, prometheusNames, valuesCopy);
    }

    /**
     * Test if these labels contain a specific label name.
     * <p>
     * Dots are treated as underscores, so {@code contains("my.label")} and {@code contains("my_label")} are the same.
     */
    public boolean contains(String labelName) {
        return get(labelName) != null;
    }

    /**
     * Get the label value for a given label name.
     * <p>
     * Returns {@code null} if the {@code labelName} is not found.
     * <p>
     * Dots are treated as underscores, so {@code get("my.label")} and {@code get("my_label")} are the same.
     */
    public String get(String labelName) {
        labelName = prometheusName(labelName);
        for (int i=0; i<prometheusNames.length; i++) {
            if (prometheusNames[i].equals(labelName)) {
                return values[i];
            }
        }
        return null;
    }

    private static void sortAndValidate(String[] names, String[] prometheusNames, String[] values) {
        sort(names, prometheusNames, values);
        validateNames(names, prometheusNames);
    }

    private static void validateNames(String[] names, String[] prometheusNames) {
        for (int i = 0; i < names.length; i++) {
            if (!isValidLabelName(names[i])) {
                throw new IllegalArgumentException("'" + names[i] + "' is an illegal label name");
            }
            // The arrays are sorted, so duplicates are next to each other
            if (i > 0 && prometheusNames[i - 1].equals(prometheusNames[i])) {
                throw new IllegalArgumentException(names[i] + ": duplicate label name");
            }
        }
    }

    private static void sort(String[] names, String[] prometheusNames, String[] values) {
        // bubblesort
        int n = prometheusNames.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (prometheusNames[j].compareTo(prometheusNames[j + 1]) > 0) {
                    swap(j, j + 1, names, prometheusNames, values);
                }
            }
        }
    }

    private static void swap(int i, int j, String[] names, String[] prometheusNames, String[] values) {
        String tmp = names[j];
        names[j] = names[i];
        names[i] = tmp;
        tmp = values[j];
        values[j] = values[i];
        values[i] = tmp;
        if (prometheusNames != names) {
            tmp = prometheusNames[j];
            prometheusNames[j] = prometheusNames[i];
            prometheusNames[i] = tmp;
        }
    }

    @Override
    public Iterator<Label> iterator() {
        return asList().iterator();
    }

    public Stream<Label> stream() {
        return asList().stream();
    }

    public int size() {
        return names.length;
    }

    public String getName(int i) {
        return names[i];
    }

    /**
     * Like {@link #getName(int)}, but dots are replaced with underscores.
     * <p>
     * This is used by Prometheus exposition formats.
     */
    public String getPrometheusName(int i) {
        return prometheusNames[i];
    }

    public String getValue(int i) {
        return values[i];
    }

    /**
     * Create a new Labels instance containing the labels of this and the labels of other.
     * This and other must not contain the same label name.
     */
    public Labels merge(Labels other) {
        if (this.isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }
        String[] names = new String[this.names.length + other.names.length];
        String[] prometheusNames = names;
        if (this.names != this.prometheusNames || other.names != other.prometheusNames) {
            prometheusNames = new String[names.length];
        }
        String[] values = new String[names.length];
        int thisPos = 0;
        int otherPos = 0;
        while (thisPos + otherPos < names.length) {
            if (thisPos >= this.names.length) {
                names[thisPos + otherPos] = other.names[otherPos];
                values[thisPos + otherPos] = other.values[otherPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = other.prometheusNames[otherPos];
                }
                otherPos++;
            } else if (otherPos >= other.names.length) {
                names[thisPos + otherPos] = this.names[thisPos];
                values[thisPos + otherPos] = this.values[thisPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = this.prometheusNames[thisPos];
                }
                thisPos++;
            } else if (this.prometheusNames[thisPos].compareTo(other.prometheusNames[otherPos]) < 0) {
                names[thisPos + otherPos] = this.names[thisPos];
                values[thisPos + otherPos] = this.values[thisPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = this.prometheusNames[thisPos];
                }
                thisPos++;
            } else if (this.prometheusNames[thisPos].compareTo(other.prometheusNames[otherPos]) > 0) {
                names[thisPos + otherPos] = other.names[otherPos];
                values[thisPos + otherPos] = other.values[otherPos];
                if (prometheusNames != names) {
                    prometheusNames[thisPos + otherPos] = other.prometheusNames[otherPos];
                }
                otherPos++;
            } else {
                throw new IllegalArgumentException("Duplicate label name: '" + this.names[thisPos] + "'.");
            }
        }
        return new Labels(names, prometheusNames, values);
    }

    /**
     * Create a new Labels instance containing the labels of this and the label passed as name and value.
     * The label name must not already be contained in this Labels instance.
     */
    public Labels add(String name, String value) {
        return merge(Labels.of(name, value));
    }

    /**
     * Create a new Labels instance containing the labels of this and the labels passed as names and values.
     * The new label names must not already be contained in this Labels instance.
     */
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
        String[] prometheusNames = makePrometheusNames(mergedNames);
        sortAndValidate(mergedNames, prometheusNames, mergedValues);
        return new Labels(mergedNames, prometheusNames, mergedValues);
    }

    public boolean hasSameNames(Labels other) {
        return Arrays.equals(prometheusNames, other.prometheusNames);
    }

    public boolean hasSameValues(Labels other) {
        return Arrays.equals(values, other.values);
    }

    @Override
    public int compareTo(Labels other) {
        int result = compare(prometheusNames, other.prometheusNames);
        if (result != 0) {
            return result;
        }
        return compare(values, other.values);
    }

    // Looks like Java doesn't have a compareTo() method for arrays.
    private int compare(String[] array1, String[] array2) {
        int result;
        for (int i = 0; i < array1.length; i++) {
            if (array2.length <= i) {
                return 1;
            }
            result = array1[i].compareTo(array2[i]);
            if (result != 0) {
                return result;
            }
        }
        if (array2.length > array1.length) {
            return -1;
        }
        return 0;
    }

    private List<Label> asList() {
        List<Label> result = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            result.add(new Label(names[i], values[i]));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * This must not be used in Prometheus exposition formats because names may contain dots.
     * <p>
     * However, for debugging it's better to show the original names rather than the Prometheus names.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                b.append(",");
            }
            b.append(names[i]);
            b.append("=\"");
            appendEscapedLabelValue(b, values[i]);
            b.append("\"");
        }
        b.append("}");
        return b.toString();
    }

    private void appendEscapedLabelValue(StringBuilder b, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    b.append("\\\\");
                    break;
                case '\"':
                    b.append("\\\"");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                default:
                    b.append(c);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Labels labels = (Labels) o;
        return labels.hasSameNames(this) && labels.hasSameValues(this);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(prometheusNames);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> names = new ArrayList<>();
        private final List<String> values = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a label. Call multiple times to add multiple labels.
         */
        public Builder label(String name, String value) {
            names.add(name);
            values.add(value);
            return this;
        }

        public Labels build() {
            return Labels.of(names, values);
        }
    }
}

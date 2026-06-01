package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/** Immutable set of name/value pairs, sorted by name. */
public final class Labels implements Comparable<Labels>, Iterable<Label> {

  public static final Labels EMPTY;

  static {
    String[] names = new String[] {};
    String[] values = new String[] {};
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

  @SuppressWarnings("ReferenceEquality")
  public boolean isEmpty() {
    return this == EMPTY || this.equals(EMPTY);
  }

  /**
   * Create a new Labels instance. You can either create Labels with one of the static {@code
   * Labels.of(...)} methods, or you can use the {@link Labels#builder()}.
   *
   * @param keyValuePairs as in {@code {name1, value1, name2, value2}}. Length must be even. {@link
   *     PrometheusNaming#isValidLabelName(String)} must be true for each name. Use {@link
   *     PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary strings to valid label
   *     names. Label names must be unique (no duplicate label names).
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
  /**
   * Create a new Labels instance. You can either create Labels with one of the static {@code
   * Labels.of(...)} methods, or you can use the {@link Labels#builder()}.
   *
   * @param names label names. {@link PrometheusNaming#isValidLabelName(String)} must be true for
   *     each name. Use {@link PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary
   *     strings to valid label names. Label names must be unique (no duplicate label names).
   * @param values label values. {@code names.size()} must be equal to {@code values.size()}.
   */
  public static Labels of(List<String> names, List<String> values) {
    if (names.size() != values.size()) {
      throw new IllegalArgumentException("Names and values must have the same size.");
    }
    if (names.isEmpty()) {
      return EMPTY;
    }
    String[] namesCopy = names.toArray(new String[0]);
    String[] valuesCopy = values.toArray(new String[0]);
    String[] prometheusNames = makePrometheusNames(namesCopy);
    sortAndValidate(namesCopy, prometheusNames, valuesCopy);
    return new Labels(namesCopy, prometheusNames, valuesCopy);
  }

  /**
   * Create a new Labels instance. You can either create Labels with one of the static {@code
   * Labels.of(...)} methods, or you can use the {@link Labels#builder()}.
   *
   * @param names label names. {@link PrometheusNaming#isValidLabelName(String)} must be true for
   *     each name. Use {@link PrometheusNaming#sanitizeLabelName(String)} to convert arbitrary
   *     strings to valid label names. Label names must be unique (no duplicate label names).
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

  static String[] makePrometheusNames(String[] names) {
    String[] prometheusNames = names;
    for (int i = 0; i < names.length; i++) {
      String name = names[i];
      if (!PrometheusNaming.isValidLegacyLabelName(name)) {
        if (prometheusNames == names) {
          prometheusNames = Arrays.copyOf(names, names.length);
        }
        prometheusNames[i] = PrometheusNaming.prometheusName(name);
      }
    }
    return prometheusNames;
  }

  /**
   * Test if these labels contain a specific label name.
   *
   * <p>Dots are treated as underscores, so {@code contains("my.label")} and {@code
   * contains("my_label")} are the same.
   */
  public boolean contains(String labelName) {
    return get(labelName) != null;
  }

  /**
   * Get the label value for a given label name.
   *
   * <p>Returns {@code null} if the {@code labelName} is not found.
   *
   * <p>Dots are treated as underscores, so {@code get("my.label")} and {@code get("my_label")} are
   * the same.
   */
  @Nullable
  public String get(String labelName) {
    labelName = prometheusName(labelName);
    for (int i = 0; i < prometheusNames.length; i++) {
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

  /**
   * Sorts all three parallel arrays in place using introspective quicksort.
   *
   * <p>Algorithm: 3-way quicksort with insertion sort for tiny partitions and heapsort fallback at
   * the recursion depth limit. Parallel arrays are swapped in lockstep.
   *
   * <p>Complexity: O(n log n) average and worst case.
   */
  private static void sort(String[] names, String[] prometheusNames, String[] values) {
    StringArraySorter.sort(names, prometheusNames, values);
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
   *
   * <p>This is used by Prometheus exposition formats.
   */
  public String getPrometheusName(int i) {
    return prometheusNames[i];
  }

  public String getValue(int i) {
    return values[i];
  }

  /**
   * Create a new Labels instance containing the labels of this and the labels of other. This and
   * other must not contain the same label name.
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
   * Create a new Labels instance containing the labels of this and the labels passed as names and
   * values. The new label names must not already be contained in this Labels instance.
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

  /**
   * Create a new Labels instance containing the labels of this and the label passed as name and
   * value. The label name must not already be contained in this Labels instance.
   */
  public Labels add(String name, String value) {
    return merge(Labels.of(name, value));
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
   *
   * <p>However, for debugging it's better to show the original names rather than the Prometheus
   * names.
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
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

    private Builder() {}

    /** Add a label. Call multiple times to add multiple labels. */
    public Builder label(String name, String value) {
      names.add(name);
      values.add(value);
      return this;
    }

    public Labels build() {
      return Labels.of(names, values);
    }
  }

  /**
   * In-place introsort for label arrays, keyed by {@code prometheusNames}.
   *
   * <p>Uses 3-way quicksort partitioning for large ranges, insertion sort for tiny ranges, and a
   * heapsort fallback at the recursion-depth limit to guarantee O(n log n) worst-case complexity.
   */
  private static final class StringArraySorter {

    private static final int INSERTION_SORT_THRESHOLD = 24;

    private static void sort(String[] names, String[] prometheusNames, String[] values) {
      int right = names.length - 1;
      if (right <= 0) {
        return;
      }
      introSort(names, prometheusNames, values, 0, right, depthLimit(names.length));
    }

    private static void introSort(
        String[] names,
        String[] prometheusNames,
        String[] values,
        int left,
        int right,
        int depthLimit) {
      while (left < right) {
        if (right - left + 1 <= INSERTION_SORT_THRESHOLD) {
          insertionSort(names, prometheusNames, values, left, right);
          return;
        }
        if (depthLimit == 0) {
          heapSort(names, prometheusNames, values, left, right);
          return;
        }
        depthLimit--;

        int mid = left + ((right - left) >>> 1);
        int pivotIndex = medianOf3(prometheusNames, left, mid, right);
        String pivot = prometheusNames[pivotIndex];

        int lt = left;
        int i = left;
        int gt = right;
        while (i <= gt) {
          int cmp = compare(prometheusNames[i], pivot);
          if (cmp < 0) {
            swap(i, lt, names, prometheusNames, values);
            i++;
            lt++;
          } else if (cmp > 0) {
            swap(i, gt, names, prometheusNames, values);
            gt--;
          } else {
            i++;
          }
        }

        if (lt - left < right - gt) {
          introSort(names, prometheusNames, values, left, lt - 1, depthLimit);
          left = gt + 1;
        } else {
          introSort(names, prometheusNames, values, gt + 1, right, depthLimit);
          right = lt - 1;
        }
      }
    }

    private static void insertionSort(
        String[] names, String[] prometheusNames, String[] values, int left, int right) {
      for (int i = left + 1; i <= right; i++) {
        String name = names[i];
        String prometheusName = prometheusNames[i];
        final String value = values[i];
        int j = i - 1;
        while (j >= left && compare(prometheusNames[j], prometheusName) > 0) {
          names[j + 1] = names[j];
          if (prometheusNames != names) {
            prometheusNames[j + 1] = prometheusNames[j];
          }
          values[j + 1] = values[j];
          j--;
        }
        names[j + 1] = name;
        if (prometheusNames != names) {
          prometheusNames[j + 1] = prometheusName;
        }
        values[j + 1] = value;
      }
    }

    private static void heapSort(
        String[] names, String[] prometheusNames, String[] values, int left, int right) {
      int size = right - left + 1;
      for (int i = (size >>> 1) - 1; i >= 0; i--) {
        siftDown(names, prometheusNames, values, left, i, size);
      }
      for (int end = size - 1; end > 0; end--) {
        swap(left, left + end, names, prometheusNames, values);
        siftDown(names, prometheusNames, values, left, 0, end);
      }
    }

    private static void siftDown(
        String[] names, String[] prometheusNames, String[] values, int base, int root, int size) {
      while (true) {
        int child = (root << 1) + 1;
        if (child >= size) {
          return;
        }
        int rightChild = child + 1;
        if (rightChild < size
            && compare(prometheusNames[base + child], prometheusNames[base + rightChild]) < 0) {
          child = rightChild;
        }
        if (compare(prometheusNames[base + root], prometheusNames[base + child]) >= 0) {
          return;
        }
        swap(base + root, base + child, names, prometheusNames, values);
        root = child;
      }
    }

    private static int depthLimit(int length) {
      int result = 0;
      while (length > 1) {
        result++;
        length >>>= 1;
      }
      return result << 1;
    }

    private static int medianOf3(String[] values, int i, int j, int k) {
      if (compare(values[i], values[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      if (compare(values[j], values[k]) > 0) {
        int tmp = j;
        j = k;
        k = tmp;
      }
      if (compare(values[i], values[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      return j;
    }

    private static int compare(String left, String right) {
      return left.compareTo(right);
    }

    private static void swap(
        int i, int j, String[] names, String[] prometheusNames, String[] values) {
      if (i == j) {
        return;
      }
      String tmp = names[i];
      names[i] = names[j];
      names[j] = tmp;
      tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
      if (prometheusNames != names) {
        tmp = prometheusNames[i];
        prometheusNames[i] = prometheusNames[j];
        prometheusNames[j] = tmp;
      }
    }
  }
}

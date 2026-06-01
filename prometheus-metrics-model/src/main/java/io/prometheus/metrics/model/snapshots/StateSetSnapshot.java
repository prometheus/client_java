package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.config.EscapingScheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/** Immutable snapshot of a StateSet metric. */
@StableApi
public final class StateSetSnapshot extends MetricSnapshot {

  /**
   * To create a new {@link StateSetSnapshot}, you can either call the constructor directly or use
   * the builder with {@link StateSetSnapshot#builder()}.
   *
   * @param metadata See {@link MetricMetadata} for more naming conventions.
   * @param data the constructor will create a sorted copy of the collection.
   */
  public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetDataPointSnapshot> data) {
    this(metadata, data, false);
    validate();
  }

  private StateSetSnapshot(
      MetricMetadata metadata, Collection<StateSetDataPointSnapshot> data, boolean internal) {
    super(metadata, data, internal);
  }

  private void validate() {
    if (getMetadata().hasUnit()) {
      throw new IllegalArgumentException("An state set metric cannot have a unit.");
    }
    for (StateSetDataPointSnapshot entry : getDataPoints()) {
      if (entry.getLabels().contains(getMetadata().getPrometheusName())) {
        throw new IllegalArgumentException(
            "Label name " + getMetadata().getPrometheusName() + " is reserved.");
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StateSetDataPointSnapshot> getDataPoints() {
    return (List<StateSetDataPointSnapshot>) dataPoints;
  }

  @SuppressWarnings("unchecked")
  @Override
  MetricSnapshot escape(
      EscapingScheme escapingScheme, List<? extends DataPointSnapshot> dataPointSnapshots) {
    return new StateSetSnapshot(
        getMetadata().escape(escapingScheme),
        (List<StateSetSnapshot.StateSetDataPointSnapshot>) dataPointSnapshots,
        true);
  }

  public static class StateSetDataPointSnapshot extends DataPointSnapshot
      implements Iterable<State> {
    final String[] names;
    final boolean[] values;

    /**
     * To create a new {@link StateSetDataPointSnapshot}, you can either call the constructor
     * directly or use the Builder with {@link StateSetDataPointSnapshot#builder()}.
     *
     * @param names state names. Must have at least 1 entry. The constructor will create a copy of
     *     the array.
     * @param values state values. Must have the same length as {@code names}. The constructor will
     *     create a copy of the array.
     * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
     */
    public StateSetDataPointSnapshot(String[] names, boolean[] values, Labels labels) {
      this(names, values, labels, 0L);
    }

    /**
     * Constructor with an additional scrape timestamp. This is only useful in rare cases as the
     * scrape timestamp is usually set by the Prometheus server during scraping. Exceptions include
     * mirroring metrics with given timestamps from other metric sources.
     */
    public StateSetDataPointSnapshot(
        String[] names, boolean[] values, Labels labels, long scrapeTimestampMillis) {
      this(names, values, labels, scrapeTimestampMillis, false);
    }

    private StateSetDataPointSnapshot(
        String[] names,
        boolean[] values,
        Labels labels,
        long scrapeTimestampMillis,
        boolean internal) {
      super(labels, 0L, scrapeTimestampMillis, false);
      if (internal) {
        this.names = names;
        this.values = values;
      } else {
        if (names.length == 0) {
          throw new IllegalArgumentException("StateSet must have at least one state.");
        }
        if (names.length != values.length) {
          throw new IllegalArgumentException("names[] and values[] must have the same length");
        }

        String[] namesCopy = Arrays.copyOf(names, names.length);
        boolean[] valuesCopy = Arrays.copyOf(values, names.length);
        sort(namesCopy, valuesCopy);
        this.names = namesCopy;
        this.values = valuesCopy;
        validate();
      }
    }

    public int size() {
      return names.length;
    }

    public String getName(int i) {
      return names[i];
    }

    public boolean isTrue(int i) {
      return values[i];
    }

    private void validate() {
      for (int i = 0; i < names.length; i++) {
        if (names[i].isEmpty()) {
          throw new IllegalArgumentException("Empty string as state name");
        }
        if (i > 0 && names[i - 1].equals(names[i])) {
          throw new IllegalArgumentException(names[i] + " duplicate state name");
        }
      }
    }

    @Override
    DataPointSnapshot escape(EscapingScheme escapingScheme) {
      return new StateSetSnapshot.StateSetDataPointSnapshot(
          names,
          values,
          SnapshotEscaper.escapeLabels(getLabels(), escapingScheme),
          getScrapeTimestampMillis(),
          true);
    }

    private List<State> asList() {
      List<State> result = new ArrayList<>(size());
      for (int i = 0; i < names.length; i++) {
        result.add(new State(names[i], values[i]));
      }
      return Collections.unmodifiableList(result);
    }

    @Override
    public Iterator<State> iterator() {
      return asList().iterator();
    }

    public Stream<State> stream() {
      return asList().stream();
    }

    /**
     * Sorts names and values in place using introspective quicksort.
     *
     * <p>Algorithm: 3-way quicksort with insertion sort for tiny partitions and heapsort fallback
     * at the recursion depth limit. Parallel arrays are swapped in lockstep.
     *
     * <p>Complexity: O(n log n) average and worst case.
     */
    private static void sort(String[] names, boolean[] values) {
      StringBooleanArraySorter.sort(names, values);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder extends DataPointSnapshot.Builder<Builder> {

      private final List<String> names = new ArrayList<>();
      private final List<Boolean> values = new ArrayList<>();

      private Builder() {}

      /** Add a state. Call multiple times to add multiple states. */
      public Builder state(String name, boolean value) {
        names.add(name);
        values.add(value);
        return this;
      }

      @Override
      protected Builder self() {
        return this;
      }

      public StateSetDataPointSnapshot build() {
        boolean[] valuesArray = new boolean[values.size()];
        for (int i = 0; i < values.size(); i++) {
          valuesArray[i] = values.get(i);
        }
        return new StateSetDataPointSnapshot(
            names.toArray(new String[] {}), valuesArray, labels, scrapeTimestampMillis);
      }
    }
  }

  public static class State {
    private final String name;
    private final boolean value;

    private State(String name, boolean value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public boolean isTrue() {
      return value;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends MetricSnapshot.Builder<Builder> {

    private final List<StateSetDataPointSnapshot> dataPoints = new ArrayList<>();

    private Builder() {}

    /** Add a data point. Call multiple times to add multiple data points. */
    public Builder dataPoint(StateSetDataPointSnapshot dataPoint) {
      dataPoints.add(dataPoint);
      return this;
    }

    @Override
    public Builder unit(@Nullable Unit unit) {
      throw new IllegalArgumentException("StateSet metric cannot have a unit.");
    }

    @Override
    public StateSetSnapshot build() {
      return new StateSetSnapshot(buildMetadata(), dataPoints);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }

  /**
   * In-place introsort for state {@code names} and parallel boolean {@code values}.
   *
   * <p>Uses 3-way quicksort partitioning for large ranges, insertion sort for tiny ranges, and a
   * heapsort fallback at the recursion-depth limit to guarantee O(n log n) worst-case complexity.
   */
  private static final class StringBooleanArraySorter {

    private static final int INSERTION_SORT_THRESHOLD = 24;

    private static void sort(String[] names, boolean[] values) {
      int right = names.length - 1;
      if (right <= 0) {
        return;
      }
      introSort(names, values, 0, right, depthLimit(names.length));
    }

    private static void introSort(
        String[] names, boolean[] values, int left, int right, int depthLimit) {
      while (left < right) {
        if (right - left + 1 <= INSERTION_SORT_THRESHOLD) {
          insertionSort(names, values, left, right);
          return;
        }
        if (depthLimit == 0) {
          heapSort(names, values, left, right);
          return;
        }
        depthLimit--;

        int mid = left + ((right - left) >>> 1);
        int pivotIndex = medianOf3(names, left, mid, right);
        String pivot = names[pivotIndex];

        int lt = left;
        int i = left;
        int gt = right;
        while (i <= gt) {
          int cmp = compare(names[i], pivot);
          if (cmp < 0) {
            swap(i, lt, names, values);
            i++;
            lt++;
          } else if (cmp > 0) {
            swap(i, gt, names, values);
            gt--;
          } else {
            i++;
          }
        }

        if (lt - left < right - gt) {
          introSort(names, values, left, lt - 1, depthLimit);
          left = gt + 1;
        } else {
          introSort(names, values, gt + 1, right, depthLimit);
          right = lt - 1;
        }
      }
    }

    private static void insertionSort(String[] names, boolean[] values, int left, int right) {
      for (int i = left + 1; i <= right; i++) {
        String name = names[i];
        boolean value = values[i];
        int j = i - 1;
        while (j >= left && compare(names[j], name) > 0) {
          names[j + 1] = names[j];
          values[j + 1] = values[j];
          j--;
        }
        names[j + 1] = name;
        values[j + 1] = value;
      }
    }

    private static void heapSort(String[] names, boolean[] values, int left, int right) {
      int size = right - left + 1;
      for (int i = (size >>> 1) - 1; i >= 0; i--) {
        siftDown(names, values, left, i, size);
      }
      for (int end = size - 1; end > 0; end--) {
        swap(left, left + end, names, values);
        siftDown(names, values, left, 0, end);
      }
    }

    private static void siftDown(String[] names, boolean[] values, int base, int root, int size) {
      while (true) {
        int child = (root << 1) + 1;
        if (child >= size) {
          return;
        }
        int rightChild = child + 1;
        if (rightChild < size && compare(names[base + child], names[base + rightChild]) < 0) {
          child = rightChild;
        }
        if (compare(names[base + root], names[base + child]) >= 0) {
          return;
        }
        swap(base + root, base + child, names, values);
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

    private static int medianOf3(String[] names, int i, int j, int k) {
      if (compare(names[i], names[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      if (compare(names[j], names[k]) > 0) {
        int tmp = j;
        j = k;
        k = tmp;
      }
      if (compare(names[i], names[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      return j;
    }

    private static int compare(String left, String right) {
      return left.compareTo(right);
    }

    private static void swap(int i, int j, String[] names, boolean[] values) {
      if (i == j) {
        return;
      }
      String name = names[i];
      names[i] = names[j];
      names[j] = name;
      boolean value = values[i];
      values[i] = values[j];
      values[j] = value;
    }
  }
}

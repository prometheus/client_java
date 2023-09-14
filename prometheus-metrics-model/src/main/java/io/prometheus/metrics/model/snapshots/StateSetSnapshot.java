package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable snapshot of a StateSet metric.
 */
public final class StateSetSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link StateSetSnapshot}, you can either call the constructor directly or use
     * the builder with {@link StateSetSnapshot#builder()}.
     *
     * @param metadata See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetDataPointSnapshot> data) {
        super(metadata, data);
        validate();
    }

    private void validate() {
        if (getMetadata().hasUnit()) {
            throw new IllegalArgumentException("An state set metric cannot have a unit.");
        }
        for (StateSetDataPointSnapshot entry : getDataPoints()) {
            if (entry.getLabels().contains(getMetadata().getPrometheusName())) {
                throw new IllegalArgumentException("Label name " + getMetadata().getPrometheusName() + " is reserved.");
            }
        }
    }

    @Override
    public List<StateSetDataPointSnapshot> getDataPoints() {
        return (List<StateSetDataPointSnapshot>) dataPoints;
    }


    public static class StateSetDataPointSnapshot extends DataPointSnapshot implements Iterable<State> {
        private final String[] names;
        private final boolean[] values;

        /**
         * To create a new {@link StateSetDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link StateSetDataPointSnapshot#builder()}.
         *
         * @param names  state names. Must have at least 1 entry.
         *               The constructor will create a copy of the array.
         * @param values state values. Must have the same length as {@code names}.
         *               The constructor will create a copy of the array.
         * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
         */
        public StateSetDataPointSnapshot(String[] names, boolean[] values, Labels labels) {
            this(names, values, labels, 0L);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public StateSetDataPointSnapshot(String[] names, boolean[] values, Labels labels, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
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
                if (names[i].length() == 0) {
                    throw new IllegalArgumentException("Empty string as state name");
                }
                if (i > 0 && names[i - 1].equals(names[i])) {
                    throw new IllegalArgumentException(names[i] + " duplicate state name");
                }
            }
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

        private static void sort(String[] names, boolean[] values) {
            // Bubblesort
            int n = names.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (names[j].compareTo(names[j + 1]) > 0) {
                        swap(j, j + 1, names, values);
                    }
                }
            }
        }

        private static void swap(int i, int j, String[] names, boolean[] values) {
            String tmpName = names[j];
            names[j] = names[i];
            names[i] = tmpName;
            boolean tmpValue = values[j];
            values[j] = values[i];
            values[i] = tmpValue;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DataPointSnapshot.Builder<Builder> {

            private final ArrayList<String> names = new ArrayList<>();
            private final ArrayList<Boolean> values = new ArrayList<>();

            private Builder() {}

            /**
             * Add a state. Call multple times to add multiple states.
             */
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
                return new StateSetDataPointSnapshot(names.toArray(new String[]{}), valuesArray, labels, scrapeTimestampMillis);
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

        private Builder() {
        }

        /**
         * Add a data point. Call multiple times to add multiple data points.
         */
        public Builder dataPoint(StateSetDataPointSnapshot dataPoint) {
            dataPoints.add(dataPoint);
            return this;
        }

        @Override
        public Builder unit(Unit unit) {
            throw new IllegalArgumentException("StateSet metric cannot have a unit.");
        }

        public StateSetSnapshot build() {
            return new StateSetSnapshot(buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

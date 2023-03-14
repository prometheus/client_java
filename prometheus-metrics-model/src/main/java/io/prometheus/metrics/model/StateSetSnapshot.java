package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class StateSetSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link StateSetSnapshot}, you can either call the constructor directly or use
     * the builder with {@link StateSetSnapshot#newBuilder()}.
     *
     * @param metadata See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetData> data) {
        super(metadata, data);
        validate();
    }

    private void validate() {
        if (getMetadata().hasUnit()) {
            throw new IllegalArgumentException("An Info metric cannot have a unit.");
        }
        for (StateSetData entry : getData()) {
            if (entry.getLabels().contains(getMetadata().getName())) {
                throw new IllegalArgumentException("Label name " + getMetadata().getName() + " is reserved.");
            }
        }
    }

    @Override
    public List<StateSetData> getData() {
        return (List<StateSetData>) data;
    }


    public static class StateSetData extends MetricData implements Iterable<State> {
        private final String[] names;
        private final boolean[] values;

        /**
         * To create a new {@link StateSetData}, you can either call the constructor directly or use the
         * Builder with {@link StateSetData#newBuilder()}.
         *
         * @param names  state names. Must have at least 1 entry.
         *               The constructor will create a copy of the array.
         * @param values state values. Must have the same length as {@code names}.
         *               The constructor will create a copy of the array.
         * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
         */
        public StateSetData(String[] names, boolean[] values, Labels labels) {
            this(names, values, labels, 0L);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this.
         * This is only useful in rare cases as the timestamp of a Prometheus metric should usually be set by the
         * Prometheus server during scraping. Exceptions include mirroring metrics with given timestamps from other
         * metric sources.
         */
        public StateSetData(String[] names, boolean[] values, Labels labels, long scrapeTimestampMillis) {
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

        @Override
        protected void validate() {
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

        public static class Builder extends MetricData.Builder<Builder> {

            private final ArrayList<String> names = new ArrayList<>();
            private final ArrayList<Boolean> values = new ArrayList<>();

            private Builder() {}

            public Builder addState(String name, boolean value) {
                names.add(name);
                values.add(value);
                return this;
            }

            @Override
            protected Builder self() {
                return this;
            }

            public StateSetData build() {
                boolean[] valuesArray = new boolean[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    valuesArray[i] = values.get(i);
                }
                return new StateSetData(names.toArray(new String[]{}), valuesArray, labels, scrapeTimestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
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

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<StateSetData> stateSetData = new ArrayList<>();

        private Builder() {
        }

        public Builder addStateSetData(StateSetData data) {
            stateSetData.add(data);
            return this;
        }

        @Override
        public Builder withUnit(Unit unit) {
            throw new IllegalArgumentException("StateSet metric cannot have a unit.");
        }

        public StateSetSnapshot build() {
            return new StateSetSnapshot(buildMetadata(), stateSetData);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}

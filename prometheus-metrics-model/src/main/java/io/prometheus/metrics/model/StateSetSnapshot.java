package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class StateSetSnapshot extends MetricSnapshot {

    public StateSetSnapshot(String name, StateSetData... data) {
        this(new MetricMetadata(name), data);
    }

    public StateSetSnapshot(String name, String help, StateSetData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public StateSetSnapshot(MetricMetadata metadata, StateSetData... data) {
        this(metadata, Arrays.asList(data));
    }

    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetData> data) {
        super(metadata, data);
        validate();
    }

    private void validate() {
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

        public StateSetData(Labels labels, String[] names, boolean[] values) {
            this(labels, names, values, 0L);
        }

        public StateSetData(Labels labels, String[] names, boolean[] values, long timestampMillis) {
            super(labels, 0L, timestampMillis);
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
        void validate() {
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
            return result;
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

        public static class Builder {
            private ArrayList<String> names = new ArrayList<>();
            private ArrayList<Boolean> values = new ArrayList<Boolean>();
            private Labels labels = Labels.EMPTY;

            private Builder() {
            }

            public Builder addState(String name, boolean value) {
                names.add(name);
                values.add(value);
                return this;
            }

            public Builder withLabels(Labels labels) {
                this.labels = labels;
                return this;
            }

            public StateSetData build() {
                boolean[] valuesArray = new boolean[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    valuesArray[i] = values.get(i);
                }
                return new StateSetData(labels, names.toArray(new String[]{}), valuesArray);
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
}

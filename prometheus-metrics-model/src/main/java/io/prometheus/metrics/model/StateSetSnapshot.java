package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class StateSetSnapshot extends MetricSnapshot {

    private final Collection<StateSetData> data;

    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetData> data) {
        super(metadata);
        this.data = data;
        validate();
    }

    private void validate() {
        for (StateSetData entry : data) {
            if (entry.getLabels().contains(getMetadata().getName())) {
                throw new IllegalArgumentException("Label name " + getMetadata().getName() + " is reserved.");
            }
        }
    }

    public Collection<StateSetData> getData() {
        return data;
    }


    public static class StateSetData extends MetricData {

        private final Map<String, Boolean> states = new HashMap<>();

        public StateSetData(Labels labels, String[] names, boolean[] values) {
            super(labels);
            if (names.length == 0) {
                throw new IllegalArgumentException("StateSet must have at least one state.");
            }
            if (names.length != values.length) {
                throw new IllegalArgumentException("Names and values must have the same number of entries");
            }
            for (int i=0; i<names.length; i++) {
                states.put(names[i], values[i]);
            }
            validate();
        }

        public Collection<State> getStates() {
            ArrayList<State> result = new ArrayList<>(states.size());
            for (Map.Entry<String, Boolean> state : states.entrySet()) {
                result.add(new State(state.getKey(), state.getValue()));
            }
            result.sort(Comparator.comparing(State::getName));
            return result;
        }

        public Boolean getState(String name) {
            return states.get(name);
        }

        @Override
        void validate() {}
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

        public boolean isValue() {
            return value;
        }
    }
}

package io.prometheus.metrics.model.snapshots;

/**
 * Utility for iterating over {@link Labels}.
 */
public final class Label implements Comparable<Label> {

    private final String name;
    private final String value;

    public Label(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(Label other) {
        int nameCompare = name.compareTo(other.name);
        return nameCompare != 0 ? nameCompare : value.compareTo(other.value);
    }
}

package io.prometheus.metrics.model.snapshots;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "Label{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(name, label.name) && Objects.equals(value, label.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}

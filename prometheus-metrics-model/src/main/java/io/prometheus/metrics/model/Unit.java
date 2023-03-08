package io.prometheus.metrics.model;

public class Unit {

    private final String name;

    public static final Unit RATIO = new Unit("ratio");
    public static final Unit SECONDS = new Unit("seconds");
    public static final Unit BYTES = new Unit("bytes");

    public static final Unit CELSIUS = new Unit("celsius");

    public Unit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

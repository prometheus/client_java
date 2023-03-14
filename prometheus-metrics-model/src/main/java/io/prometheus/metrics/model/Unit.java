package io.prometheus.metrics.model;

/**
 * In Prometheus, units are largely based on SI base units.
 * Base units include seconds, bytes, joules, grams, meters, ratios, volts, amperes, and celsius.
 * This class provides predefined units for convenience.
 * If none of these work, you can create your own units with
 * <pre>
 *     new Unit("myUnit");
 * </pre>
 */
public class Unit {

    private final String name;

    public static final Unit RATIO = new Unit("ratio");
    public static final Unit SECONDS = new Unit("seconds");
    public static final Unit BYTES = new Unit("bytes");
    public static final Unit CELSIUS = new Unit("celsius");
    public static final Unit JOULES = new Unit("joules");
    public static final Unit GRAMS = new Unit("grams");
    public static final Unit METERS = new Unit("meters");
    public static final Unit VOLTS = new Unit("volts");
    public static final Unit AMPERES = new Unit("amperes");

    public Unit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

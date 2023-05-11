package io.prometheus.metrics.model.snapshots;

/**
 * Some pre-defined units for convenience. You can create your own units with
 * <pre>
 *     new Unit("myUnit");
 * </pre>
 * Note that in Prometheus, units are largely based on SI base units
 * (seconds, bytes, joules, grams, meters, ratio, volts, amperes, and celsius).
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
        if (name == null) {
            throw new NullPointerException("Unit name cannot be null.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Unit name cannot be empty.");
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public static double nanosToSeconds(long nanos) {
        return nanos / 1E9;
    }

    public static double millisToSeconds(long nanos) {
        return nanos / 1E3;
    }
}

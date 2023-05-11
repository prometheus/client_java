package io.prometheus.metrics.model.snapshots;

/**
 * Immutable representation of a Quantile.
 */
public class Quantile {

    private final double quantile;
    private final double value;

    /**
     * @param quantile expecting 0.0 &lt;= quantile &lt;= 1.0, otherwise an {@link IllegalArgumentException} will be thrown.
     * @param value
     */
    public Quantile(double quantile, double value) {
        this.quantile = quantile;
        this.value = value;
        validate();
    }

    public double getQuantile() {
        return quantile;
    }

    public double getValue() {
        return value;
    }

    private void validate() {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException(quantile + ": Illegal quantile. Expecting 0 <= quantile <= 1");
        }
    }
}

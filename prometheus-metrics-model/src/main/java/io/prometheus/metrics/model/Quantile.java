package io.prometheus.metrics.model;

public class Quantile {
    private final double quantile;
    private final double value;

    public Quantile(double quantile, double value) {
        this.quantile = quantile;
        this.value = value;
    }

    public double getQuantile() {
        return quantile;
    }

    public double getValue() {
        return value;
    }
}

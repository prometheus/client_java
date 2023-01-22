package io.prometheus.metrics.model;

import java.util.*;

public class Quantiles implements Iterable<Quantile> {
    private final List<Quantile> quantiles;
    public static final Quantiles EMPTY = new Quantiles(Collections.emptyList());

    private Quantiles(List<Quantile> quantiles) {
        quantiles = new ArrayList<>(quantiles);
        Collections.sort(quantiles);
        this.quantiles = Collections.unmodifiableList(quantiles);
    }

    public static Quantiles of(List<Quantile> quantiles) {
        return new Quantiles(quantiles);
    }

    public static Quantiles of(Quantile... quantiles) {
        return of(Arrays.asList(quantiles));
    }

    @Override
    public Iterator<Quantile> iterator() {
        return quantiles.iterator();
    }

    public static class Builder {

        private List<Quantile> quantiles = new ArrayList<>();
        private Builder() {}

        public Builder addQuantile(Quantile quantile) {
            quantiles.add(quantile);
            return this;
        }

        public Builder addQuantile(double quantile, double value) {
            quantiles.add(new Quantile(quantile, value));
            return this;
        }

        public Quantiles build() {
            return new Quantiles(quantiles);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}

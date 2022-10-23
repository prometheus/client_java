package io.prometheus.metrics.model;

import java.util.*;

public class Quantiles implements Iterable<Quantile> {
    private final List<Quantile> quantiles;

    private Quantiles(List<Quantile> quantiles) {
        quantiles = new ArrayList<>(quantiles);
        Collections.sort(quantiles);
        this.quantiles = Collections.unmodifiableList(quantiles);
    }

    public static Quantiles of(List<Quantile> quantiles) {
        return new Quantiles(quantiles);
    }

    @Override
    public Iterator<Quantile> iterator() {
        return quantiles.iterator();
    }
}

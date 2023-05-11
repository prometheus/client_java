package io.prometheus.metrics.model.snapshots;

import java.util.*;

/**
 * Immutable list of quantiles.
 */
public class Quantiles implements Iterable<Quantile> {

    private final List<Quantile> quantiles;
    public static final Quantiles EMPTY = new Quantiles(Collections.emptyList());

    private Quantiles(List<Quantile> quantiles) {
        quantiles = new ArrayList<>(quantiles);
        quantiles.sort(Comparator.comparing(Quantile::getQuantile));
        this.quantiles = Collections.unmodifiableList(quantiles);
        validate();
    }

    private void validate() {
        for (int i=0; i< quantiles.size() - 1; i++) {
            if (quantiles.get(i).getQuantile() == quantiles.get(i+1).getQuantile()) {
                throw new IllegalArgumentException("Duplicate " + quantiles.get(i).getQuantile() + " quantile.");
            }
        }
    }

     /**
     * Create a new Quantiles instance.
     * You can either create Quantiles with one of the static {@code Quantiles.of(...)} methods,
     * or you can use the {@link Quantiles#newBuilder()}.
     */
    public static Quantiles of(List<Quantile> quantiles) {
        return new Quantiles(quantiles);
    }

    /**
     * Create a new Quantiles instance.
     * You can either create Quantiles with one of the static {@code Quantiles.of(...)} methods,
     * or you can use the {@link Quantiles#newBuilder()}.
     */
    public static Quantiles of(Quantile... quantiles) {
        return of(Arrays.asList(quantiles));
    }

    public int size() {
        return quantiles.size();
    }

    public Quantile get(int i) {
        return quantiles.get(i);
    }

    @Override
    public Iterator<Quantile> iterator() {
        return quantiles.iterator();
    }

    public static class Builder {

        private final List<Quantile> quantiles = new ArrayList<>();
        private Builder() {}

        public Builder addQuantile(Quantile quantile) {
            quantiles.add(quantile);
            return this;
        }

        /**
         * @param quantile 0.0 &lt;= quantile &lt;= 1.0
         */
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

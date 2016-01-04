package io.prometheus.client;

/**
 * Created by warebot on 12/27/15.
 */
/**
 * <p>
 * Quantile is an invariant for estimation with {@link StreamCopy}.
 * </p>
 */
public class Quantile {
    final double quantile;
    final double error;
    final double u;
    final double v;

    /**
     * <p>
     * Create an invariant for a quantile
     * </p>
     *
     * @param quantile The target quantile value expressed along the interval
     *        <code>[0, 1]</code>.
     * @param error The target error allowance expressed along the interval
     *        <code>[0, 1]</code>.
     */
    public Quantile(final double quantile, final double error) {
        this.quantile = quantile;
        this.error = error;
        u = 2.0 * error / (1.0 - quantile);
        v = 2.0 * error / quantile;
    }

    @Override
    public String toString() {
        return String.format("Q{q=%f, eps=%f})", quantile, error);
    }

    double delta(final double rank, final int n) {
        if (rank <= Math.floor(quantile * n)) {
            return u * (n - rank);
        }
        return v * rank;
    }

    public double getQuantile() {
        return quantile;
    }

    public double getError() {
        return error;
    }
}
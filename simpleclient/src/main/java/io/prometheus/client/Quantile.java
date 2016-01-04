package io.prometheus.client;

/**
 * Created by warebot on 12/27/15.
 */

/**
 * <p>
 * Quantile is an invariant for estimation with {@link Stream}.
 * </p>
 */


public class Quantile {
    final double quantile;
    final double error;


    /**
     * <p>
     * Create an invariant for a quantile
     * </p>
     *
     * @param quantile The target quantile value expressed along the interval
     *                 <code>[0, 1]</code>.
     * @param error    The target error allowance expressed along the interval
     *                 <code>[0, 1]</code>.
     */
    public Quantile(final double quantile, final double error) {
        this.quantile = quantile;
        this.error = error;
    }


    @Override
    public String toString() {
        return String.format("Q{q=%f, eps=%f})", quantile, error);
    }

    public double getQuantile() {
        return quantile;
    }

    public double getError() {
        return error;
    }

    public double f(double r, int n) {
        if (r <= Math.floor(quantile * n)) {
            return (2.0 * error * (n - r)) / (1.0 - quantile);
        } else {
            return (2.0 * error * r) / quantile;
        }
    }

}





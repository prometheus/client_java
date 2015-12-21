package io.prometheus.client;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by warebot on 12/21/15.
 */
public class Reservoir {
    private double[] values;
    private DoubleAdder count = new DoubleAdder();

    public void update(double val) {
        count.add(1);
        int c = count.intValue();
        if (count.intValue() <= values.length) {
            values[c - 1] = val;
        } else {
            final long r = nextLong(c);
            if (r < values.length) {
                values[(int) r] = val;
            }
        }
    }

    public Reservoir(int capacity) {
        values = new double[capacity];
    }

    private static long nextLong(long n) {
        long bits, val;
        do {
            bits = ThreadLocalRandom.current().nextLong() & (~(1L << 63));
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }

    public double[] getSnapshot() {
        int capacity = Math.min(this.count.intValue(), values.length);
        return Arrays.copyOfRange(values, 0, capacity);
    }


    public double getQuantile(double quantile, double[] data) {
        if (quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        int size = data.length;

        if (size == 0) {
            return 0.0;
        }

        final double pos = quantile * (size);
        final Boolean wholeNumber = pos == Math.floor(pos) && !Double.isInfinite(pos);
        final int index = wholeNumber ? (int) pos - 1 : (int) Math.ceil(pos) - 1;

        if (index < 1) {
            return data[0];
        }

        if (index >= size) {
            return data[size - 1];
        }

        if (wholeNumber) {
            if (size >= index + 1) {
                return (data[index] + data[index + 1]) / 2;
            }
            return data[index];
        } else {
            return data[index];
        }
    }
}

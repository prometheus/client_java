package io.prometheus.client;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

final class DoubleHistograms {

    private DoubleHistograms() {

    }

    private static final class ExclusiveDoubleHistogram extends AtomicLongArray implements DoubleHistogram {

        private static final AtomicLongFieldUpdater<ExclusiveDoubleHistogram> SUM_UPDATER = AtomicLongFieldUpdater.newUpdater(ExclusiveDoubleHistogram.class, "sum");

        private final double[] upperBounds;
        private volatile long sum;

        public ExclusiveDoubleHistogram(double[] buckets) {
            super(buckets.length);
            upperBounds = buckets;
            sum = Double.doubleToRawLongBits(0d);
        }

        @Override
        public void observe(double amt) {
            for (int i = 0; i < upperBounds.length; ++i) {
                // The last bucket is +Inf, so we always increment.
                if (amt <= upperBounds[i]) {
                    final double value = Double.longBitsToDouble(get(i));
                    final double sum = value + 1d;
                    lazySet(i, Double.doubleToRawLongBits(sum));
                    break;
                }
            }
            SUM_UPDATER.lazySet(this, Double.doubleToRawLongBits(observationsSum() + amt));
        }

        @Override
        public int buckets() {
            return length();
        }

        @Override
        public double observationsSum() {
            return Double.longBitsToDouble(sum);
        }

        @Override
        public int observedValues(double[] values) {
            final int size = length();
            double acc = 0;
            for (int i = 0; i < size; i++) {
                acc += Double.longBitsToDouble(get(i));
                values[i] = acc;
            }
            return size;
        }
    }

    private static final class SharedDoubleHistogram implements DoubleHistogram {

        private final double[] upperBounds;
        private final DoubleAdder[] cumulativeCounts;
        private final DoubleAdder sum = new DoubleAdder();

        public SharedDoubleHistogram(double[] buckets) {
            this.upperBounds = buckets;
            cumulativeCounts = new DoubleAdder[buckets.length];
            for (int i = 0; i < buckets.length; ++i) {
                cumulativeCounts[i] = new DoubleAdder();
            }
        }

        @Override
        public void observe(double amt) {
            for (int i = 0; i < upperBounds.length; ++i) {
                // The last bucket is +Inf, so we always increment.
                if (amt <= upperBounds[i]) {
                    cumulativeCounts[i].add(1);
                    break;
                }
            }
            sum.add(amt);
        }

        @Override
        public int buckets() {
            return cumulativeCounts.length;
        }

        @Override
        public double observationsSum() {
            return sum.sum();
        }

        @Override
        public int observedValues(double[] buckets) {
            double acc = 0;
            for (int i = 0; i < cumulativeCounts.length; ++i) {
                acc += cumulativeCounts[i].sum();
                buckets[i] = acc;
            }
            return cumulativeCounts.length;
        }
    }


    public static DoubleHistogram shared(double[] buckets) {
        return new SharedDoubleHistogram(buckets);

    }

    public static DoubleHistogram exclusive(double[] buckets) {
        return new ExclusiveDoubleHistogram(buckets);
    }


}

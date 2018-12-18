package io.prometheus.client;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

final class DoubleCounters {

    private static final class SharedDoubleCounter extends DoubleAdder implements DoubleCounter {
    }

    private static final class ExclusiveDoubleCounter implements DoubleCounter {

        private static final AtomicLongFieldUpdater<ExclusiveDoubleCounter> VALUE_UPDATER = AtomicLongFieldUpdater.newUpdater(ExclusiveDoubleCounter.class, "value");

        private volatile long value;

        ExclusiveDoubleCounter(double initialValue) {
            this.value = Double.doubleToRawLongBits(initialValue);
        }

        ExclusiveDoubleCounter() {
            this(0);
        }

        @Override
        public double sum() {
            return Double.longBitsToDouble(value);
        }

        @Override
        public long longValue() {
            return (long) sum();
        }

        @Override
        public void add(double delta) {
            double v = sum();
            double add = v + delta;
            final long longBits = Double.doubleToRawLongBits(add);
            VALUE_UPDATER.lazySet(this, longBits);
        }

        @Override
        public double doubleValue() {
            return sum();
        }

        @Override
        public int intValue() {
            return (int) sum();
        }

        @Override
        public float floatValue() {
            return (float) sum();
        }

        @Override
        public void reset() {
            VALUE_UPDATER.lazySet(this, 0L);
        }
    }


    private DoubleCounters() {

    }

    public static DoubleCounter shared() {
        return new SharedDoubleCounter();
    }

    public static DoubleCounter exclusive() {
        return new ExclusiveDoubleCounter();
    }
}

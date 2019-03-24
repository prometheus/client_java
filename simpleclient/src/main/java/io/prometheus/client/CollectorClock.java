package io.prometheus.client;

public interface CollectorClock {

    CollectorClock DEFAULT_CLOCK = new DefaultClock();

    long millis();

    long nanos();

    class DefaultClock implements CollectorClock {

        DefaultClock() {}

        @Override
        public long millis() {
            return System.currentTimeMillis();
        }

        @Override
        public long nanos() {
            return System.nanoTime();
        }
    }

}

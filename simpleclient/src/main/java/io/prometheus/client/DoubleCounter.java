package io.prometheus.client;

interface DoubleCounter {

    double sum();

    long longValue();

    double doubleValue();

    int intValue();

    float floatValue();

    void add(double delta);

    void reset();
}

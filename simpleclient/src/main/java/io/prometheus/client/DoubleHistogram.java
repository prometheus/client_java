package io.prometheus.client;

interface DoubleHistogram {

    void observe(double amt);

    int buckets();

    double observationsSum();

    int observedValues(double[] values);
}

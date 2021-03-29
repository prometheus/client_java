package io.prometheus.client.exemplars.api;

/**
 * Lazy value holder for a double value.
 * <p/>
 * Counters and Gauges track their current value using a DoubleAdder, so getting the value
 * might be expensive in a highly multithreaded application.
 * Passing the value via this lazy value holder allows Exemplar samplers to access the value only if needed.
 */
public interface Value {

  double get();
}

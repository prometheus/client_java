package io.prometheus.metrics;

/**
 * Replacement for Java 8's {@code java.util.function.Supplier} for compatibility with Java versions &lt; 8.
 */
public interface Supplier<T> {
    T get();
}

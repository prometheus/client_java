package io.prometheus.client;

/**
 * Replacement for Java 8's {@code java.util.function.Predicate} for compatibility with Java versions &lt; 8.
 */
public interface Predicate<T> {
    boolean test(T t);
}
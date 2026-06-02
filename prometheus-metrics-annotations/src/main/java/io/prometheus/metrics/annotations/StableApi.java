package io.prometheus.metrics.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a Java element as part of the stable, published Prometheus Metrics API.
 *
 * <p>Use this on public or protected types to publish the type and its members. Use it on
 * individual constructors, methods, and fields when only part of a public type is stable.
 */
@Documented
@Retention(CLASS)
@Target({TYPE, CONSTRUCTOR, METHOD, FIELD})
public @interface StableApi {}

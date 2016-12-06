package io.prometheus.client.spring.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Spring-AOP-based automated method timing for the annotated method or all methods in an annotated class.
 * The timings will be recorded in a Histogram with the metric name of {@code prometheus_method_timing}, and a label
 * of {@code signature} whose values will be {@code ClassOrInterfaceName.methodName(..)}.
 *
 * @author Andrew Stuart <andrew.stuart2@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PrometheusTimeMethods {
    /**
     * The metric name to use for recording latencies
     * @return A metric name specific to your use case
     */
    String value() default "";

    /**
     * The help messsage to show in prometheus metrics
     * @return A help string
     */
    String help() default "Automatic annotation-driven method timing";
}

package io.prometheus.client.spring.boot;

import org.springframework.context.annotation.Import;

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

}

package io.prometheus.client.spring.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Spring-AOP-based automated method timing for the annotated method or all methods in an annotated class.
 * The timings will be recorded in a Histogram with the default metric name of {@code prometheus_method_timing},
 * which can be overridden by providing a {@code value} parameter in the annotation, and optionally a {@code help}
 * parameter and a single label of {@code signature} whose values will be {@code ClassOrInterfaceName.methodName(..)}.
 *
 * To properly work, {@link EnablePrometheusTiming} must be specified somewhere in your application configuration.
 *
 *  <pre><code>
 * {@literal @}PrometheusTimeMethods("my_app_timer_seconds")
 * {@literal @}Controller
 *  public class MyController {
 *    {@literal @}RequestMapping("/")
 *    {@literal @}ResponseBody
 *    public Object handleRequest() {
 *      // Each invocation will be timed and recorded.
 *      return database.withCache().get("some_data");
 *    }
 *  }
 * </code></pre>
 *
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

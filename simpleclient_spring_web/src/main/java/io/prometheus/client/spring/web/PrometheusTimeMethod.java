package io.prometheus.client.spring.web;

import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Spring-AOP-based automated method timing for the annotated method. The timings will be recorded in a
 * {@link io.prometheus.client.Summary} or a {@link io.prometheus.client.Histogram } with a name specified by the
 * required {@code name} parameter, and help specified by the {@code help} parameter. The default collector is a
 * Summary. Histograms use default bucketing.
 *
 * To properly work, {@link EnablePrometheusTiming} must be specified somewhere in your application configuration.
 *
 *  <pre><code>
 * {@literal @}Controller
 *  public class MyController {
 *    {@literal @}RequestMapping("/")
 *    {@literal @}ResponseBody
 *    {@literal @}PrometheusTimeMethod(name = "my_method_seconds", help = "The number of seconds taken by the main handler", collectorClass = Summary.class)
 *    public Object handleRequest() {
 *      // Each invocation will be timed and recorded.
 *      return database.withCache().get("some_data");
 *    }
 *  }
 * </code></pre>
 *
 *
 * @author Andrew Stuart
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PrometheusTimeMethod {
    /**
     * The metric name to use for recording latencies
     * @return A metric name specific to your use case.
     */
    String name();

    /**
     * The help message to show in prometheus metrics
     * @return A help string
     */
    String help();

    /**
     * Defines the collector class to be used to capture metrics. Supports Summary (default) and Histogram.
     * @return Collector class
     */
    Class<? extends SimpleCollector> collectorClass() default Summary.class;
}

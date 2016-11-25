package io.prometheus.client.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@literal @}Enable style annotation that provides some standard
 * Prometheus metrics Collectors and integrats with standard Spring
 * PublicMetrics.
 *
 * <pre><code>
 *{@literal @}Configuration
 *{@literal @}EnablePrometheusMetrics(hotspot = true, spring = true)
 * public class MyConfiguration {
 *   ...
 * }
 * </code></pre>
 *
 * @author Stuart Williams (pidster)
 */
@Documented
@Import(EnablePrometheusMetricsImportSelector.class)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnablePrometheusMetrics {

  /**
   * @return jvm is monitored
   */
  boolean hotspot() default true;

  /**
   * @return public metrics are monitored
   */
  boolean spring() default true;

}

package io.prometheus.client.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@literal @}Enable style annotation that provides CollectorRegistry related functions.
 *
 * <pre><code>
 *{@literal @}Configuration
 *{@literal @}EnablePrometheusCollectorRegistration(useDefault = true, register = true)
 * public class MyConfiguration {
 *
 *  {@literal @}Bean
 *   public Counter counter() {
 *     return new Counter.Builder()
 *       .namespace("testnamespace")
 *       .subsystem("testsubsystem")
 *       .name("testname")
 *       .help("Help for testname")
 *       .create();
 *   }
 *
 * }
 * </code></pre>
 *
 * @author Stuart Williams (pidster)
 */
@Documented
@Import(EnablePrometheusCollectorRegistrationRegistrar.class)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnablePrometheusCollectorRegistration {

  /**
   * @return use the static default CollectorRegistry, or create a fresh instance
   */
  boolean useDefault() default true;

  /**
   * @return automatically register beans implementing Collector with the registry
   */
  boolean registerCollectors() default true;

}

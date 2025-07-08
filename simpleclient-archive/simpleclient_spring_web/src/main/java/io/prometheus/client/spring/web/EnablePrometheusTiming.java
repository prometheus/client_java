package io.prometheus.client.spring.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enable the use of {@link PrometheusTimeMethod} annotation on classes or methods.
 *
 * <p>Usage: Add this annotation to any Spring {@link
 * org.springframework.context.annotation.Configuration} class to enable the use of the {@link
 * PrometheusTimeMethod} annotation.
 *
 * <p>*
 *
 * <pre><code>
 * {@literal @}Configuration
 * {@literal @}EnablePrometheusEndpoint
 * {@literal @}EnablePrometheusTiming
 *  public class MyAppConfig {
 *    // Other configuration items...
 *  }
 * </code></pre>
 *
 * @author Andrew Stuart
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MethodTimer.class)
@Documented
public @interface EnablePrometheusTiming {}

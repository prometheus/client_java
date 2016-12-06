package io.prometheus.client.spring.web;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the use of {@link PrometheusTimeMethods} annotation on classes or methods.
 *
 * Usage: Add this annotation to any Spring {@link org.springframework.context.annotation.Configuration} class to enable
 * the use of the {@link PrometheusTimeMethods} annotation.
 *
 *  * <pre><code>
 * {@literal @}SpringBootApplication
 * {@literal @}EnablePrometheusEndpoint
 * {@literal @}EnablePrometheusTiming
 *  public class Application {
 *    public static void main(String[] args) {
 *      SpringApplication.run(Application.class, args);
 *    }
 *  }
 * </code></pre>
 *
 * @author Andrew Stuart <andrew.stuart2@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MethodTimer.class)
@Documented
public @interface EnablePrometheusTiming {}
package io.prometheus.client.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enable spring rest template metrics collector
 * <p>
 * Usage:
 * <br>Just add this annotation to the main class of your Spring Boot application, e.g.:
 * <pre>
 * {@code
 * {@literal @}SpringBootApplication
 * {@literal @}EnableRestTemplateMetricsCollector
 *  public class Application {
 *
 *    public static void main(String[] args) {
 *      SpringApplication.run(Application.class, args);
 *    }
 *  }
 * }
 * </pre>
 *
 * @author Cenk Akin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrometheusMetricsConfiguration.class)
public @interface EnableRestTemplateMetricsCollector {

}

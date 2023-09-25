package io.prometheus.client.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable an endpoint that exposes Prometheus metrics from its default collector.
 * <p>
 * Usage:
 * <br>Just add this annotation to the main class of your Spring Boot application, e.g.:
 * <pre><code>
 * {@literal @}SpringBootApplication
 * {@literal @}EnablePrometheusEndpoint
 *  public class Application {
 *
 *    public static void main(String[] args) {
 *      SpringApplication.run(Application.class, args);
 *    }
 *  }
 * </code></pre>
 * <p>
 * Configuration:
 * <br>You can customize this endpoint at runtime using the following spring properties:
 * <ul>
 * <li>{@code endpoints.prometheus.id} (default: "prometheus")</li>
 * <li>{@code endpoints.prometheus.enabled} (default: {@code true})</li>
 * <li>{@code endpoints.prometheus.sensitive} (default: {@code true})</li>
 * </ul>
 *
 * @author Marco Aust
 * @author Eliezio Oliveira
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrometheusEndpointConfiguration.class)
public @interface EnablePrometheusEndpoint {

}

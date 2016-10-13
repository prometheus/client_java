package io.prometheus.client.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable an endpoint to expose Prometheus metrics from its default collector.
 *
 * <p/>
 * Usage:
 * <br/>Just add this annotation to your main Spring Boot Application.
 *
 * <p/>
 * Configuration:
 * <br/>This endpoint is configurable through the following environment variables:
 * <li>
 *   <ul>{@code endpoints.prometheus.id} (default: "prometheus")</ul>
 *   <ul>{@code endpoints.prometheus.enabled} (default: {@code true})</ul>
 *   <ul>{@code endpoints.prometheus.sensitive} (default: {@code true})</ul>
 * </li>
 * @author Marco Aust
 * @author Eliezio Oliveira
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrometheusConfiguration.class)
public @interface EnablePrometheusEndpoint {

}

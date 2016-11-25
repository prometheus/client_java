package io.prometheus.client.spring.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;


/**
 * @deprecated in favour of using {@link io.prometheus.client.spring.EnablePrometheusMetrics}
 * and {@link io.prometheus.client.spring.EnablePrometheusCollectorRegistration} which provide
 * finer grained control and use a more conventional Spring style.
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrometheusMetricsConfiguration.class)
public @interface EnableSpringBootMetricsCollector {

}

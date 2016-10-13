package io.prometheus.client.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PrometheusConfiguration.class)
public @interface EnablePrometheusEndpoint {

}

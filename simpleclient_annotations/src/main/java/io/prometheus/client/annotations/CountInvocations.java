package io.prometheus.client.annotations;

import static io.prometheus.client.annotations.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CountInvocations {
    String namespace() default "";
    String name() default METHOD_NAME_TO_LOWER_UNDERSCORE + "_total";
    String help() default "No description";
    String[] labelNames() default {};
    LabelMapper[] labelMappers() default {};
}

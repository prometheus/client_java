package io.prometheus.client.annotations;

import static io.prometheus.client.annotations.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CountCompletions {
    String namespace() default "";
    String name() default METHOD_NAME_TO_LOWER_UNDERSCORE + "_completed_total";
    String help() default "No description";
    String[] labelNames() default {};
    LabelMapper[] labelMappers() default {};
}

package io.prometheus.client;

import static io.prometheus.client.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CountExceptions {
    String namespace() default "";
    String name() default METHOD_NAME_TO_LOWER_UNDERSCORE + "_exceptions_total";
    String help() default "No description";
    Class[] exceptionTypes() default {};
    String[] labelNames() default {};
    LabelMapper[] labelMappers() default {};
}

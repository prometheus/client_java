package io.prometheus.client;

import static io.prometheus.client.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Summarize {
    String namespace() default "";
    String name() default METHOD_NAME_TO_LOWER_UNDERSCORE + "_total";
    String help() default "No description";
    double[] quantiles() default {0.5, 0.95, 0.99, 1.};
    double[] quantileErrors() default {0.05, 0.01, 0.001, 0.001};
    String[] labelNames() default {};
    LabelMapper[] labelMappers() default {};
}

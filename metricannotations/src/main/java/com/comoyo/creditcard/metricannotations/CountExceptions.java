package com.comoyo.creditcard.metricannotations;

import static com.comoyo.creditcard.metricannotations.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CountExceptions {
    String namespace() default "";
    String name() default METHOD_NAME_TO_LOWER_UNDERSCORE + "_exceptions_total";
    String help() default "No description";
    Class[] exceptionTypes() default {};
}

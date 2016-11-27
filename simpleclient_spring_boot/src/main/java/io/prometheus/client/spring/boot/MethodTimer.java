package io.prometheus.client.spring.boot;

import io.prometheus.client.Histogram;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Created by andrew on 11/24/16.
 */
@Aspect
@ControllerAdvice
@Component
public class MethodTimer {
    public static final String METRIC_NAME = "prometheus_method_timing";

    public static final Histogram hist = Histogram.build()
            .name(METRIC_NAME)
            .help("Automatic method timing")
            .labelNames("signature")
            .register();

    @Around("within(@PrometheusMethodTiming *) || execution(@PrometheusMethodTiming * *.*(..))")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        Histogram.Timer t = hist.labels(pjp.getSignature().toShortString()).startTimer();
        try {
            Object result = pjp.proceed();
            t.observeDuration();
            return result;
        } catch (Throwable exception) {
            t.observeDuration();
            throw exception;
        }
    }
}

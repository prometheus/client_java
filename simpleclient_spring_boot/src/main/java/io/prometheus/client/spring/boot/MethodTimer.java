package io.prometheus.client.spring.boot;

import io.prometheus.client.Histogram;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * This class automatically times (via aspectj) the execution of methods by their signature, if it's been enabled via {@link EnablePrometheusTiming}
 * and in methods annotated (or within classes annotated) with {@link PrometheusTimeMethods}
 *
 * @author Andrew Stuart <andrew.stuart2@gmail.com>
 */
@Aspect
@ControllerAdvice
@Component
public class MethodTimer {
    public static final String METRIC_NAME = "prometheus_method_timing";

    public static final Histogram hist = Histogram.build()
            .name(METRIC_NAME)
            .help("Automatic annotation-driven method timing")
            .labelNames("signature")
            .register();

    @Around("within(@PrometheusTimeMethods *) || execution(@PrometheusTimeMethods * *.*(..))")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        Histogram.Timer t = hist.labels(pjp.getSignature().toShortString()).startTimer();
        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

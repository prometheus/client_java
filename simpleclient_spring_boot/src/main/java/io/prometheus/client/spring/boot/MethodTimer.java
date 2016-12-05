package io.prometheus.client.spring.boot;

import io.prometheus.client.Summary;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;

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
    public static final String METRIC_NAME = "prometheus_method_timing_seconds";

    public static final HashMap<String, Summary> collectors = new HashMap<String, Summary>(10);

    public static final Summary defaultSummary = Summary.build()
            .name(METRIC_NAME)
            .help("Automatic annotation-driven method timing")
            .labelNames("signature")
            .register();

    @Around("(within(@PrometheusTimeMethods *) || execution(@PrometheusTimeMethods * *.*(..))) && @annotation(annot)")
    public Object timeMethod(ProceedingJoinPoint pjp, PrometheusTimeMethods annot) throws Throwable {

        Summary summary = defaultSummary;
        String name = annot.value();
        if (!StringUtils.isEmpty(name)) {
            if (collectors.containsKey(name)) {
                summary = collectors.get(name);
            } else {
                summary = Summary.build()
                        .name(name)
                        .help(annot.help())
                        .labelNames("signature")
                        .register();
            }
        }

        Summary.Timer t = summary.labels(pjp.getSignature().toShortString()).startTimer();
        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

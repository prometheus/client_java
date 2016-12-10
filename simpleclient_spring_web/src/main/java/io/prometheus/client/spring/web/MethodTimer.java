package io.prometheus.client.spring.web;

import io.prometheus.client.Summary;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * This class automatically times (via aspectj) the execution of methods by their signature, if it's been enabled via {@link EnablePrometheusTiming}
 * and in methods annotated (or within classes annotated) with {@link PrometheusTimeMethods}
 *
 * @author Andrew Stuart
 */
@Aspect("pertarget(io.prometheus.client.spring.web.MethodTimer.timeable())")
@ControllerAdvice
@Component
public class MethodTimer {
    public static final String DEFAULT_METRIC_NAME = "spring_web_method_timing_seconds";

    public static final Summary defaultSummary = Summary.build()
            .name(DEFAULT_METRIC_NAME)
            .help("Automatic annotation-driven method timing")
            .labelNames("signature")
            .register();

    private Summary summary = null;

    @Pointcut("@within(io.prometheus.client.spring.web.PrometheusTimeMethods)")
    public void annotatedClass() {}

    @Pointcut("@annotation(io.prometheus.client.spring.web.PrometheusTimeMethods)")
    public void annotatedMethod() {}

    @Pointcut("annotatedClass() || annotatedMethod()")
    public void timeable() {}

    private PrometheusTimeMethods getAnnotation(ProceedingJoinPoint pjp) {
        assert(pjp.getSignature() instanceof MethodSignature);

        MethodSignature signature = (MethodSignature) pjp.getSignature();

        PrometheusTimeMethods annot =  AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), PrometheusTimeMethods.class);
        if (annot != null) {
            return annot;
        }

        try {
            // When target is an AOP interface proxy but annotation is on class method.
            final String name = signature.getName();
            final Class[] parameterTypes = signature.getParameterTypes();

            return AnnotationUtils.findAnnotation(pjp.getTarget().getClass().getDeclaredMethod(name, parameterTypes), PrometheusTimeMethods.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized private void ensureSummary(ProceedingJoinPoint pjp) {
        if (summary != null) {
            return;
        }

        PrometheusTimeMethods annot = getAnnotation(pjp);

        if (StringUtils.isEmpty(annot.value())) {
            summary = defaultSummary;
            return;
        }

        // Only one thread may get here, as writeLock is fully mutually exclusive
        summary = Summary.build()
                .name(annot.value())
                .help(annot.help())
                .labelNames("signature")
                .register();
    }

    @Around("timeable()")
    public Object timeClassMethod(ProceedingJoinPoint pjp) throws Throwable {
        if (summary == null) {
            ensureSummary(pjp);
        }

        Summary.Timer t = summary.labels(pjp.getSignature().toShortString()).startTimer();

        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

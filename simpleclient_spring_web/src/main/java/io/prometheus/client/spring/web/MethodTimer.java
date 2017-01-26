package io.prometheus.client.spring.web;

import io.prometheus.client.Summary;
import org.apache.commons.lang3.ObjectUtils;
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
    private Summary summary = null;
    private Summary.Child summaryChild = null;

    @Pointcut("@within(io.prometheus.client.spring.web.PrometheusTimeMethods)")
    public void annotatedClass() {}

    @Pointcut("@annotation(io.prometheus.client.spring.web.PrometheusTimeMethods)")
    public void annotatedMethod() {}

    @Pointcut("annotatedMethod()")
    public void timeable() {}

    private PrometheusTimeMethods getAnnotation(ProceedingJoinPoint pjp) throws NoSuchMethodException {
        assert(pjp.getSignature() instanceof MethodSignature);

        MethodSignature signature = (MethodSignature) pjp.getSignature();

        PrometheusTimeMethods annot =  AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), PrometheusTimeMethods.class);
        if (annot != null) {
            return annot;
        }

        // When target is an AOP interface proxy but annotation is on class method (rather than Interface method).
        final String name = signature.getName();
        final Class[] parameterTypes = signature.getParameterTypes();

        return AnnotationUtils.findAnnotation(pjp.getTarget().getClass().getDeclaredMethod(name, parameterTypes), PrometheusTimeMethods.class);
    }

    synchronized private void ensureSummary(ProceedingJoinPoint pjp) throws IllegalStateException {
        // Guard against multiple concurrent readers who see `summary == null` and call ensureSummary
        if (summaryChild != null) {
            return;
        }

        // Only one thread may get here, since this method is synchronized
        PrometheusTimeMethods annot = null;
        try {
            annot = getAnnotation(pjp);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        }

        assert(annot != null);

        summaryChild = Summary.build()
                .name(annot.name())
                .help(annot.help())
                .register().labels();
//                .labelNames("signature")
//                .register().labels(pjp.getSignature().toShortString());
    }

    @Around("timeable()")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        // This is not thread safe itself, but faster. The critical section within `ensureSummary` makes a second check
        // so that the summary is only created once.
        if (summaryChild == null) {
            ensureSummary(pjp);
        }

        final Summary.Timer t = summaryChild.startTimer();

        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

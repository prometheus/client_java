package io.prometheus.client.spring.web;

import io.prometheus.client.Summary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;

/**
 * This class automatically times (via aspectj) the execution of annotated methods, if it's been enabled via {@link EnablePrometheusTiming},
 * for methods annotated with {@link PrometheusTimeMethod}
 *
 * @author Andrew Stuart
 */
@Aspect("pertarget(io.prometheus.client.spring.web.MethodTimer.timeable())")
@Scope("prototype")
@ControllerAdvice
public class MethodTimer {
    private HashMap<String, Summary> summaries = new HashMap<String, Summary>();

    @Pointcut("@within(io.prometheus.client.spring.web.PrometheusTimeMethod)")
    public void annotatedClass() {}

    @Pointcut("@annotation(io.prometheus.client.spring.web.PrometheusTimeMethod)")
    public void annotatedMethod() {}

    @Pointcut("annotatedMethod() || annotatedClass()")
    public void timeable() {}

    private PrometheusTimeMethod getAnnotation(ProceedingJoinPoint pjp) throws NoSuchMethodException {
        assert(pjp.getSignature() instanceof MethodSignature);
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        PrometheusTimeMethod annot = AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), PrometheusTimeMethod.class);
        if (annot != null) {
            return annot;
        }

        // When target is an AOP interface proxy but annotation is on class method (rather than Interface method).
        final String name = signature.getName();
        final Class[] parameterTypes = signature.getParameterTypes();

        return AnnotationUtils.findAnnotation(pjp.getTarget().getClass().getDeclaredMethod(name, parameterTypes), PrometheusTimeMethod.class);
    }

    private String hashKey(ProceedingJoinPoint pjp) {
        return pjp.getSignature().toLongString();
    }

    synchronized private Summary ensureSummary(ProceedingJoinPoint pjp) throws IllegalStateException {
        // Guard against multiple concurrent readers who see `summaryChild == null` and call ensureSummary
        String longSig = hashKey(pjp);
        Summary summary = summaries.get(longSig);
        if (summary != null) {
            return summary;
        }

        // Only one thread may get here, since this method is synchronized
        PrometheusTimeMethod annot;
        try {
            annot = getAnnotation(pjp);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        }

        assert(annot != null);

        summary = Summary.build()
                .name(annot.name())
                .help(annot.help())
                .register();

        summaries.put(longSig, summary);

        return summary;
    }

    @Around("timeable()")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        // This is not thread safe itself, but faster. The critical section within `ensureSummary` makes a second check
        // so that the summaries is only created once.
        Summary summary = summaries.get(hashKey(pjp));
        if (summary == null) {
            summary = ensureSummary(pjp);
        }

        final Summary.Timer t = summary.startTimer();

        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

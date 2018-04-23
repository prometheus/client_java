package io.prometheus.client.spring.web;

import io.prometheus.client.Summary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private final ReadWriteLock summaryLock = new ReentrantReadWriteLock();
    private final HashMap<String, Summary> summaries = new HashMap<String, Summary>();

    @Pointcut("@annotation(io.prometheus.client.spring.web.PrometheusTimeMethod)")
    public void annotatedMethod() {}

    @Pointcut("annotatedMethod()")
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
        Method method = ReflectionUtils.findMethod(pjp.getTarget().getClass(), name, parameterTypes);
        return AnnotationUtils.findAnnotation(method, PrometheusTimeMethod.class);
    }

    private Summary ensureSummary(ProceedingJoinPoint pjp, String key) throws IllegalStateException {
        PrometheusTimeMethod annot;
        try {
            annot = getAnnotation(pjp);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        }

        assert(annot != null);

        Summary summary;

        // We use a writeLock here to guarantee no concurrent reads.
        final Lock writeLock = summaryLock.writeLock();
        writeLock.lock();
        try {
            // Check one last time with full mutual exclusion in case multiple readers got null before creation.
            summary = summaries.get(key);
            if (summary != null) {
                return summary;
            }

            // Now we know for sure that we have never before registered.
            summary = Summary.build()
                    .name(annot.name())
                    .help(annot.help())
                    .register();

            // Even a rehash of the underlying table will not cause issues as we mutually exclude readers while we
            // perform our updates.
            summaries.put(key, summary);

            return summary;
        } finally {
            writeLock.unlock();
        }
    }

    @Around("timeable()")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        String key = pjp.getSignature().toLongString();

        Summary summary;
        final Lock r = summaryLock.readLock();
        r.lock();
        try {
            summary = summaries.get(key);
        } finally {
            r.unlock();
        }

        if (summary == null) {
            summary = ensureSummary(pjp, key);
        }

        final Summary.Timer t = summary.startTimer();

        try {
            return pjp.proceed();
        } finally {
            t.observeDuration();
        }
    }
}

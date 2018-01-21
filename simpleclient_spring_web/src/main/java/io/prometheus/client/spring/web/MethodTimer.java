package io.prometheus.client.spring.web;

import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.lang.reflect.InvocationTargetException;
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
    private final ReadWriteLock collectorLock = new ReentrantReadWriteLock();
    private final HashMap<String, SimpleCollector> collectors = new HashMap<String, SimpleCollector>();

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

        return AnnotationUtils.findAnnotation(pjp.getTarget().getClass().getDeclaredMethod(name, parameterTypes), PrometheusTimeMethod.class);
    }

    private SimpleCollector ensureCollector(ProceedingJoinPoint pjp, String key) throws IllegalStateException {
        PrometheusTimeMethod annot;
        try {
            annot = getAnnotation(pjp);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Annotation could not be found for pjp \"" + pjp.toShortString() +"\"", e);
        }

        assert(annot != null);

        SimpleCollector simpleCollector;

        // We use a writeLock here to guarantee no concurrent reads.
        final Lock writeLock = collectorLock.writeLock();
        writeLock.lock();
        try {
            // Check one last time with full mutual exclusion in case multiple readers got null before creation.
            simpleCollector = collectors.get(key);
            if (simpleCollector != null) {
                return simpleCollector;
            }

            try {
                SimpleCollector.Builder builder = (SimpleCollector.Builder) annot.collectorClass()
                        .getMethod("build").invoke(null);

                // Now we know for sure that we have never before registered.
                simpleCollector = builder
                        .name(annot.name())
                        .help(annot.help())
                        .register();

                // Even a rehash of the underlying table will not cause issues as we mutually exclude readers while we
                // perform our updates.
                collectors.put(key, simpleCollector);

                return simpleCollector;
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new IllegalArgumentException("Invalid collectorClass specified. Only Summary and " +
                        "Histogram collectors are supported for PrometheusTimedMethod collection.", noSuchMethodException);
            } catch (IllegalAccessException illegalAccessException) {
                throw new IllegalArgumentException("Invalid collectorClass specified. Only Summary and " +
                        "Histogram collectors are supported for PrometheusTimedMethod collection.", illegalAccessException);
            } catch (InvocationTargetException invocationTargetException) {
                throw new IllegalArgumentException("Invalid collectorClass specified. Only Summary and " +
                        "Histogram collectors are supported for PrometheusTimedMethod collection.", invocationTargetException);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Around("timeable()")
    public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
        String key = pjp.getSignature().toLongString();

        SimpleCollector collector;
        final Lock r = collectorLock.readLock();
        r.lock();
        try {
            collector = collectors.get(key);
        } finally {
            r.unlock();
        }

        if (collector == null) {
            collector = ensureCollector(pjp, key);
        }

        if (collector.getClass().equals(Summary.class)) {
            final Summary.Timer t = ((Summary) collector).startTimer();

            try {
                return pjp.proceed();
            } finally {
                t.observeDuration();
            }
        } else if (collector.getClass().equals(Histogram.class)) {
            final Histogram.Timer t = ((Histogram) collector).startTimer();

            try {
                return pjp.proceed();
            } finally {
                t.observeDuration();
            }
        } else {
            throw new IllegalStateException("Unsupported Collector class: " + collector.getClass().getCanonicalName());
        }
    }
}

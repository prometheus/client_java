package io.prometheus.client;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PrometheusMonitor<T> implements InvocationHandler {
    private final T manager;
    private final String className;

    public static final Map<String, Counter> COUNTERS = new HashMap<>();
    public static final Map<String, Summary> SUMMARIES = new HashMap<>();
    public static final String METHOD_NAME_TO_LOWER_UNDERSCORE
            = "<method-name-to-lower-underscore>";

    public static <T> T monitor(final T manager) {
        final Class<?> classObject = manager.getClass();
        return (T) Proxy.newProxyInstance(
                classObject.getClassLoader(),
                classObject.getInterfaces(),
                new PrometheusMonitor<T>(manager));
    }

    private PrometheusMonitor(final T manager) {
        this.manager = manager;
        final String simpleName = manager.getClass().getSimpleName();
        final String nameWithoutMockitoPostfix = simpleName.split("\\$\\$")[0];
        className = UPPER_CAMEL.to(LOWER_UNDERSCORE, nameWithoutMockitoPostfix);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        final Summary summary = summarizeDuration(method);
        final Instant startTime = summary == null ? null : Instant.now();
        try {
            countInvocation(method);
            final Object result = method.invoke(manager, args);
            countComplete(method);
            return result;
        } catch (final InvocationTargetException e) {
            countException(e, method);
            throw e.getCause();
        } finally {
            if (summary != null) {
                summary.observe(calculateDuration(startTime));
            }
        }
    }

    private Summary summarizeDuration(final Method method) {
        final Summarize annotation = getAnnotation(method, Summarize.class);
        if (annotation != null) {
            final String counterName = expandCounterName(annotation.name(), method);
            return SUMMARIES.computeIfAbsent(annotation.namespace() + counterName, value ->
                    createSummary(counterName, annotation));
        }
        return null;
    }

    private Summary createSummary(final String counterName, final Summarize annotation) {
        final Summary.Builder summary = Summary.build()
                .namespace(annotation.namespace())
                .name(counterName)
                .help(annotation.help());
        for (int i = 0; i < annotation.quantiles().length; i++) {
            summary.quantile(annotation.quantiles()[i], annotation.quantileErrors()[i]);
        }
        return summary.register();
    }

    private void countInvocation(final Method method) {
        final CountInvocations annotation = getAnnotation(method, CountInvocations.class);
        if (annotation != null) {
            final String counterName = expandCounterName(annotation.name(), method);
            COUNTERS.computeIfAbsent(
                    annotation.namespace() + counterName,
                    value -> Counter.build()
                            .namespace(annotation.namespace())
                            .name(counterName)
                            .help(annotation.help())
                            .register())
                    .inc();
        }
    }

    private <T extends Annotation> T getAnnotation(final Method method, final Class<T> type) {
        T annotation = getMethod(manager.getClass(), method).getAnnotation(type);
        final Class<?>[] interfaces = manager.getClass().getInterfaces();
        for (int i = 0; annotation == null && i < interfaces.length; i++) {
            annotation = interfaces[i].getAnnotation(type);
            if (annotation == null) {
                annotation = getMethod(interfaces[i], method).getAnnotation(type);
            }
        }
        return annotation;
    }

    private Method getMethod(final Class<?> aClass, final Method method) {
        try {
            return aClass.getMethod(method.getName());
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    private String expandCounterName(final String name, final Method method) {
        final String toLowercaseKey = METHOD_NAME_TO_LOWER_UNDERSCORE;
        if (name.contains(toLowercaseKey)) {
            return name.replace(toLowercaseKey, LOWER_CAMEL.to(LOWER_UNDERSCORE, method.getName()));
        }
        return name;
    }

    private void countComplete(final Method method) {
        final CountCompletions annotation = getAnnotation(method, CountCompletions.class);
        if (annotation != null) {
            final String counterName = expandCounterName(annotation.name(), method);
            COUNTERS.computeIfAbsent(annotation.namespace() + counterName, value -> Counter.build()
                    .namespace(annotation.namespace())
                    .name(counterName)
                    .help(annotation.help())
                    .register())
                    .inc();
        }
    }

    private void countException(final InvocationTargetException e, final Method method) {
        final CountExceptions annotation = getAnnotation(method, CountExceptions.class);
        if (isCorrectException(e.getTargetException(), annotation)) {
            final String counterName = expandCounterName(annotation.name(), method);
            COUNTERS.computeIfAbsent(annotation.namespace() + counterName, value -> Counter.build()
                    .namespace(annotation.namespace())
                    .name(counterName)
                    .help(annotation.help())
                    .register())
                    .inc();
        }
    }

    private boolean isCorrectException(
            final Throwable exception,
            final CountExceptions annotation) {
        if (annotation == null) {return false;}
        final Class[] exceptionTypes = annotation.exceptionTypes();
        if (exceptionTypes.length == 0) {return true;}
        for (Class exceptionType : exceptionTypes) {
            if (exceptionType.isInstance(exception)) {
                return true;
            }
        }
        return false;
    }

    private static double calculateDuration(final Instant start) {
        return Duration.between(start, Instant.now()).toMillis() / 1000d;
    }
};

package io.prometheus.client.annotations;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrometheusMonitor<T> implements InvocationHandler {
    private final T manager;
    private final String className;

    public static final Map<String, Counter> COUNTERS = new ConcurrentHashMap<>();
    public static final Map<String, Summary> SUMMARIES = new ConcurrentHashMap<>();
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
        final String readableName = removeMockitoPostfix(manager.getClass().getSimpleName());
        className = UPPER_CAMEL.to(LOWER_UNDERSCORE, readableName);
    }

    public static String removeMockitoPostfix(final String simpleName) {
        return simpleName.split("\\$\\$")[0];
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        final SummaryTimer summary = new SummaryTimer(method);
        Object result = null;
        try {
            countInvocation(method);
            result = method.invoke(manager, args);
            countComplete(method, result);
        } catch (final InvocationTargetException e) {
            countException(e, method);
            throw e.getCause();
        } finally {
            if (summary.isStarted()) {
                summary.observe(method, result);
            }
        }
        return result;
    }

    private class SummaryTimer {
        private final Summary summary;
        private final Instant startTime;
        private final LabelMapper[] labelMappers;

        private SummaryTimer(final Method method) {
            final Summarize annotation = getDefiningAnnotation(method, Summarize.class);
            if (annotation != null) {
                startTime = Instant.now();
                final String counterName = expandCounterName(annotation.name(), method);
                summary = SUMMARIES.computeIfAbsent(annotation.namespace() + counterName, value ->
                        createSummary(counterName, annotation));
                labelMappers = annotation.labelMappers();
            } else {
                summary = null;
                startTime = null;
                labelMappers = null;
            }
        }

        private Summary createSummary(final String counterName, final Summarize annotation) {
            final Summary.Builder summary = Summary.build()
                    .namespace(annotation.namespace())
                    .name(counterName)
                    .help(annotation.help())
                    .labelNames(annotation.labelNames());
            for (int i = 0; i < annotation.quantiles().length; i++) {
                summary.quantile(annotation.quantiles()[i], annotation.quantileErrors()[i]);
            }
            return summary.register();
        }

        boolean isStarted() {
            return summary != null;
        }

        void observe(final Method method, final Object result) {
            String[] labels = (String[]) Arrays.stream(labelMappers)
                    .map(mapper -> mapper.getLabel(manager, method, result))
                    .toArray(String[]::new);
            summary.labels(labels)
                    .observe(calculateDuration(startTime));
        }

        private double calculateDuration(final Instant start) {
            return Duration.between(start, Instant.now()).toMillis() / 1000d;
        }
    }

    private void countInvocation(final Method method) {
        final CountInvocations annotation = getDefiningAnnotation(method, CountInvocations.class);
        if (annotation != null) {
            final String counterName = expandCounterName(annotation.name(), method);
            String[] labels = (String[]) Arrays.stream(annotation.labelMappers())
                    .map(mapper -> mapper.getLabel(manager, method, null))
                    .toArray(String[]::new);
            COUNTERS.computeIfAbsent(
                    annotation.namespace() + counterName,
                    value -> Counter.build()
                            .namespace(annotation.namespace())
                            .name(counterName)
                            .help(annotation.help())
                            .labelNames(annotation.labelNames())
                            .register())
                    .labels(labels)
                    .inc();
        }
    }

    private <T extends Annotation> T getDefiningAnnotation(
            final Method method,
            final Class<T> type) {
        final Class<?> managerClass = manager.getClass();
        T annotation = getAnnotation(managerClass, method, type);
        final Class<?>[] interfaces = managerClass.getInterfaces();
        for (int i = 0; annotation == null && i < interfaces.length; i++) {
            annotation = interfaces[i].getAnnotation(type);
            if (annotation == null) {
                annotation = getAnnotation(interfaces[i], method, type);
            }
        }
        return annotation;
    }

    private <T extends Annotation> T getAnnotation(
            final Class<?> aClass,
            final Method method,
            final Class<T> type) {
        final String methodName = removeMockitoPostfix(method.getName());
        for (final Method methodToMatch : aClass.getMethods()) {
            if (removeMockitoPostfix(methodToMatch.getName()).equals(methodName)) {
                return methodToMatch.getAnnotation(type);
            }
        }
        return null;
    }

    private String expandCounterName(final String name, final Method method) {
        if (name.contains(METHOD_NAME_TO_LOWER_UNDERSCORE)) {
            final String leadingName = removeMockitoPostfix(method.getName());
            return name.replace(
                    METHOD_NAME_TO_LOWER_UNDERSCORE,
                    LOWER_CAMEL.to(LOWER_UNDERSCORE, leadingName));
        }
        return name;
    }

    private void countComplete(final Method method, final Object result) {
        final CountCompletions annotation = getDefiningAnnotation(method, CountCompletions.class);
        if (annotation != null) {
            final String counterName = expandCounterName(annotation.name(), method);
            String[] labels = (String[]) Arrays.stream(annotation.labelMappers())
                    .map(mapper -> mapper.getLabel(manager, method, result))
                    .toArray(String[]::new);
            COUNTERS.computeIfAbsent(annotation.namespace() + counterName, value -> Counter.build()
                    .namespace(annotation.namespace())
                    .name(counterName)
                    .help(annotation.help())
                    .labelNames(annotation.labelNames())
                    .register())
                    .labels(labels)
                    .inc();
        }
    }

    private void countException(final InvocationTargetException e, final Method method) {
        final CountExceptions annotation = getDefiningAnnotation(method, CountExceptions.class);
        if (isCorrectException(e.getTargetException(), annotation)) {
            final String counterName = expandCounterName(annotation.name(), method);
            String[] labels = (String[]) Arrays.stream(annotation.labelMappers())
                    .map(mapper -> mapper.getLabel(manager, method, e.getCause()))
                    .toArray(String[]::new);
            COUNTERS.computeIfAbsent(annotation.namespace() + counterName, value -> Counter.build()
                    .namespace(annotation.namespace())
                    .name(counterName)
                    .help(annotation.help())
                    .labelNames(annotation.labelNames())
                    .register())
                    .labels(labels)
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
};

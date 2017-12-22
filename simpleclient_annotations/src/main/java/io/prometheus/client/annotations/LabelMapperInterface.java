package io.prometheus.client.annotations;

import java.lang.reflect.Method;

public interface LabelMapperInterface {
    String getLabel(final Object instance, final Method method, final Object result);
}

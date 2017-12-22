package io.prometheus.client.annotations;

import java.lang.reflect.Method;

public interface LabelMapperInterface {
    String getLabel(final Method method, final Throwable e, final Object result);
}

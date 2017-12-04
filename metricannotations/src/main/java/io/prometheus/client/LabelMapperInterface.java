package io.prometheus.client;

import java.lang.reflect.Method;

public interface LabelMapperInterface {
    String getLabel(final Method method, final Throwable e);
}

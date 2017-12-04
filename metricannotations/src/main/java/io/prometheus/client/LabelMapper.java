package io.prometheus.client;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.lang.reflect.Method;

public enum LabelMapper implements LabelMapperInterface {
    METHOD_NAME {
        @Override
        public String getLabel(final Method method, final Throwable e) {
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, method.getName());
        }
    },
    CLASS_NAME {
        @Override
        public String getLabel(final Method method, final Throwable e) {
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, method.getDeclaringClass().getSimpleName());
        }
    },
    EXCEPTION_TYPE {
        @Override
        public String getLabel(final Method method, final Throwable e) {
            return UPPER_CAMEL.to(LOWER_UNDERSCORE, e.getClass().getSimpleName());
        }
    }
}

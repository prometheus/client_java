package io.prometheus.client.annotations;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.lang.reflect.Method;

public enum LabelMapper implements LabelMapperInterface {
    METHOD_NAME {
        @Override
        public String getLabel(final Object instance, final Method method, final Object result){
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, method.getName());
        }
    },
    CLASS_NAME {
        @Override
        public String getLabel(final Object instance, final Method method, final Object result){
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, instance.getClass().getSimpleName());
        }
    },
    EXCEPTION_TYPE {
        @Override
        public String getLabel(final Object instance, final Method method, final Object result){
            return UPPER_CAMEL.to(LOWER_UNDERSCORE, result.getClass().getSimpleName());
        }
    },
    CUSTOM_EXCEPTION_LABEL {
        @Override
        public String getLabel(final Object instance, final Method method, final Object error){
            try {
                return (String) method.getDeclaringClass()
                        .getMethod("getLabel", Throwable.class)
                        .invoke(null, error);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    },
    CUSTOM_RESULT_LABEL {
        @Override
        public String getLabel(final Object instance, final Method method, final Object result){
            try {
                return (String) method.getDeclaringClass()
                        .getMethod("getResultLabel", Object.class)
                        .invoke(null, result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    },
    RESULT_TO_STRING {
        @Override
        public String getLabel(final Object instance, final Method method, final Object result){
            if (result == null) {
                return "";
            }
            return result.toString();
        }
    }
}

package com.comoyo.creditcard.metricannotations;
import static com.comoyo.creditcard.metricannotations.PrometheusMonitor.METHOD_NAME_TO_LOWER_UNDERSCORE;
import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import org.junit.Test;

public class PrometheusMonitorExceptionTest extends MetricsTest {
    @CountExceptions
    public interface ThrowingExceptions {
        void throwRuntimeException();
        void throwIOException() throws IOException;
    }

    private final ThrowingExceptions interfaceAnnotations
            = PrometheusMonitor.monitor(new ThrowingExceptions() {
        @Override
        public void throwRuntimeException() {
            throw new RuntimeException("error");
        }

        @Override
        public void throwIOException() throws IOException {
            throw new IOException("error");
        }
    });

    private final ThrowingExceptions methodAnnotations
            = PrometheusMonitor.monitor(new ThrowingExceptions() {
        @Override
        @CountExceptions(
                namespace="namespace",
                name="prefix_" + METHOD_NAME_TO_LOWER_UNDERSCORE,
                help="strange counter",
                exceptionTypes = {RuntimeException.class})
        public void throwRuntimeException() {
            throw new RuntimeException("error");
        }

        @Override
        @CountExceptions(
                namespace="second_namespace",
                name="my_counter_name",
                help="second exception counter",
                exceptionTypes = {IOException.class})
        public void throwIOException() throws IOException{
            throw new IOException("error");
        }
    });

    private final ThrowingExceptions wrongExceptionTypes
            = PrometheusMonitor.monitor(new ThrowingExceptions() {
        @Override
        @CountExceptions(namespace="wrong", exceptionTypes = {IOException.class})
        public void throwRuntimeException() {
            throw new RuntimeException("error");
        }

        @Override
        @CountExceptions(namespace="wrong", exceptionTypes = {RuntimeException.class})
        public void throwIOException() throws IOException {
            throw new IOException("error");
        }
    });

    @Test
    public void testExceptionCounter() throws Exception {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                interfaceAnnotations.throwRuntimeException());
        assertThat(defaultRegistry.getSampleValue("throw_runtime_exception_exceptions_total"))
                .isEqualTo(1);
    }

    @Test
    public void testCheckedExceptionCounter() throws Exception {
        assertThatExceptionOfType(IOException.class).isThrownBy(() ->
                interfaceAnnotations.throwIOException());
        assertThat(defaultRegistry.getSampleValue(
                "throw_i_o_exception_exceptions_total")).isEqualTo(1);
    }

    @Test
    public void testMethodAnnotationsOnRuntimeException() throws Exception {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            methodAnnotations.throwRuntimeException());
        assertThat(defaultRegistry.getSampleValue("namespace_prefix_throw_runtime_exception"))
                .isEqualTo(1);
    }

    @Test
    public void testMethodAnnotationsOnCheckedException() throws Exception {
        assertThatExceptionOfType(IOException.class).isThrownBy(() ->
                methodAnnotations.throwIOException());
        assertThat(defaultRegistry.getSampleValue("second_namespace_my_counter_name")).isEqualTo(1);
    }

    @Test
    public void testWrongExceptionTypeDoesNotCountItOnRuntimeException() throws Exception {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                wrongExceptionTypes.throwRuntimeException());
        assertThat(defaultRegistry.getSampleValue("wrong_throw_runtime_exception_exceptions_total"))
                .isNull();
    }

    @Test
    public void testWrongExceptionTypeDoesNotCountItOnCheckedException() throws Exception {
        assertThatExceptionOfType(IOException.class).isThrownBy(() ->
                wrongExceptionTypes.throwIOException());
        assertThat(defaultRegistry.getSampleValue(
                "wrong_throw_i_o_exception_exceptions_total")).isNull();
    }
}

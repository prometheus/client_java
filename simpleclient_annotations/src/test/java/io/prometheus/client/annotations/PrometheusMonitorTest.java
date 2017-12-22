package io.prometheus.client.annotations;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static io.prometheus.client.annotations.LabelMapper.CLASS_NAME;
import static io.prometheus.client.annotations.LabelMapper.CUSTOM_RESULT_LABEL;
import static io.prometheus.client.annotations.LabelMapper.METHOD_NAME;
import static io.prometheus.client.annotations.LabelMapper.RESULT_TO_STRING;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.offset;

import org.junit.Test;

public class PrometheusMonitorTest extends MetricsTest {
    public interface OneFunction {
        @CountInvocations
        @CountCompletions
        @Summarize(namespace = "summary")
        String theFunction();
        static String getResultLabel(Object result) {
            return ((String)result).substring(1);
        }
    }

    private final OneFunction annotated = () -> "";
    private final OneFunction throwing = new OneFunction() {
        @Override
        @CountInvocations(namespace = "exception")
        @CountCompletions(namespace = "exception")
        @Summarize(namespace = "exception_summary")
        public String theFunction() { throw new RuntimeException("error"); }
    };

    public class Duration implements OneFunction {
        public long milliseconds = 10L;
        @Override
        @CountInvocations(namespace = "duration")
        @CountCompletions(namespace = "duration")
        @Summarize(namespace = "duration_summary")
        public String theFunction() {
            try {
                sleep(milliseconds);
            } catch (InterruptedException e) {
            }
            return "";
        }
    }
    private final Duration duration = new Duration();

    public static class CustomLabels implements OneFunction {
        @Override
        @CountInvocations(
                namespace = "custom",
                labelNames = {"method_name", "class_name"},
                labelMappers = {METHOD_NAME, CLASS_NAME})
        @CountCompletions(
                namespace = "custom",
                labelNames = {"method_name", "class_name", "custom_result", "string"},
                labelMappers = {METHOD_NAME, CLASS_NAME, CUSTOM_RESULT_LABEL, RESULT_TO_STRING})
        @Summarize(
                namespace = "custom_summary",
                labelNames = {"method_name", "class_name", "result"},
                labelMappers = {METHOD_NAME, CLASS_NAME, RESULT_TO_STRING})
        public String theFunction() {return "result";}
    }
    private final OneFunction customLabels = new CustomLabels();

    public interface NotAnnotated {

        void run();
    }
    private final NotAnnotated notAnnotated = () -> {};

    @Test
    public void testAnnotatedMethod() throws Exception {
        final OneFunction monitor = PrometheusMonitor.monitor(annotated);
        monitor.theFunction();
        assertThat(defaultRegistry.getSampleValue("the_function_total")).isEqualTo(1);
        assertThat(defaultRegistry.getSampleValue("the_function_completed_total")).isEqualTo(1);
        assertThat(defaultRegistry.getSampleValue("summary_the_function_total_count")).isEqualTo(1);
        monitor.theFunction();
        assertThat(defaultRegistry.getSampleValue("the_function_total")).isEqualTo(2);
        assertThat(defaultRegistry.getSampleValue("the_function_completed_total")).isEqualTo(2);
        assertThat(defaultRegistry.getSampleValue("summary_the_function_total_count")).isEqualTo(2);
        assertThat(defaultRegistry.getSampleValue("summary_the_function_total_sum"))
                .isBetween(0.0, 0.1);
    }

    @Test
    public void testException() throws Exception {
        final OneFunction monitor = PrometheusMonitor.monitor(throwing);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> monitor.theFunction());
        assertThat(defaultRegistry.getSampleValue("exception_the_function_total")).isEqualTo(1);
        assertThat(defaultRegistry.getSampleValue("exception_the_function_completed_total"))
                .isNull();
    }

    @Test
    public void testTiming() throws Exception {
        final OneFunction monitor = PrometheusMonitor.monitor(duration);
        duration.milliseconds = 100L;
        monitor.theFunction();
        assertThat(defaultRegistry.getSampleValue("duration_summary_the_function_total_count"))
                .isEqualTo(1);
        assertThat(defaultRegistry.getSampleValue("duration_summary_the_function_total_sum"))
                .isBetween(0.1, 0.2);
        duration.milliseconds = 10L;
        monitor.theFunction();
        duration.milliseconds = 300L;
        monitor.theFunction();
        assertThat(defaultRegistry.getSampleValue(
                "duration_summary_the_function_total",
                new String[]{"quantile"},
                new String[]{"1.0"})).isEqualTo(.3, offset(.1));
        assertThat(defaultRegistry.getSampleValue(
                "duration_summary_the_function_total",
                new String[]{"quantile"},
                new String[]{"0.99"})).isEqualTo(.1, offset(.1));
    }

    @Test
    public void testNotAnnotatedDoesNotThrowException() {
        PrometheusMonitor.monitor(notAnnotated).run();
    }

    @Test
    public void testCustomLabels() throws Exception {
        PrometheusMonitor.monitor(customLabels).theFunction();
        assertThat(defaultRegistry.getSampleValue(
                "custom_the_function_total",
                new String[]{"method_name", "class_name"},
                new String[]{"the_function", "custom_labels"})).isEqualTo(1);
        assertThat(defaultRegistry.getSampleValue(
                "custom_the_function_completed_total",
                new String[]{"method_name", "class_name", "custom_result", "string"},
                new String[]{"the_function", "custom_labels", "esult", "result"})).isEqualTo(1);

        assertThat(defaultRegistry.getSampleValue(
                "custom_summary_the_function_total_count",
                new String[]{"method_name", "class_name", "result"},
                new String[]{"the_function", "custom_labels", "result"})).isEqualTo(1);
    }
}

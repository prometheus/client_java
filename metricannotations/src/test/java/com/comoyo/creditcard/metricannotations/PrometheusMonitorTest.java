package com.comoyo.creditcard.metricannotations;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
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
        void theFunction();
    }

    private final OneFunction annotated = () -> {};
    private final OneFunction throwing = new OneFunction() {
        @Override
        @CountInvocations(namespace = "exception")
        @CountCompletions(namespace = "exception")
        @Summarize(namespace = "exception_summary")
        public void theFunction() { throw new RuntimeException("error"); }
    };

    public class Duration implements OneFunction {
        public long milliseconds = 10L;
        @Override
        @CountInvocations(namespace = "duration")
        @CountCompletions(namespace = "duration")
        @Summarize(namespace = "duration_summary")
        public void theFunction() {
            try {
                sleep(milliseconds);
            } catch (InterruptedException e) {
            }
        }
    };
    private final Duration duration = new Duration();

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
}

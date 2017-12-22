package io.prometheus.client.annotations;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrometheusMonitorMockitoCompatibilityTest extends MetricsTest {

    @CountInvocations(namespace = "mocked")
    interface InterfaceToMock {
        void method();
    }

    @Mock
    private InterfaceToMock mock;

    @Test
    public void testAnnotationWorksOnTheMock() throws Exception {
        final InterfaceToMock monitor = PrometheusMonitor.monitor(mock);
        monitor.method();
        assertThat(defaultRegistry.getSampleValue("mocked_method_total")).isEqualTo(1);
    }
}

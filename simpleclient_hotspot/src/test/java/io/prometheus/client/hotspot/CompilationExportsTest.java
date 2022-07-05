package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.CompilationMXBean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CompilationExportsTest {

    private CompilationMXBean mockCompilationsBean = Mockito.mock(CompilationMXBean.class);
    private CollectorRegistry registry = new CollectorRegistry();
    private CompilationExports collectorUnderTest;

    private static final String[] EMPTY_LABEL = new String[0];

    @Before
    public void setUp() {
        when(mockCompilationsBean.getTotalCompilationTime()).thenReturn(10l);
        when(mockCompilationsBean.isCompilationTimeMonitoringSupported()).thenReturn(true);
        collectorUnderTest = new CompilationExports(mockCompilationsBean).register(registry);
    }

    @Test
    public void testCompilation() {
        assertEquals(
                10.0,
                registry.getSampleValue(
                        "jvm_compilation_time_ms_total", EMPTY_LABEL, EMPTY_LABEL),
                .0000001);
    }
}
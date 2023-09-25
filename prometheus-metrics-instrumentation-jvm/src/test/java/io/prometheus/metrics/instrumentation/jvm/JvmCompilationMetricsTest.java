package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.SampleNameFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.CompilationMXBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class CompilationExportsTest {

    private CompilationMXBean mockCompilationsBean = Mockito.mock(CompilationMXBean.class);
    private CollectorRegistry registry = new CollectorRegistry();
    private CompilationExports collectorUnderTest;

    private static final String[] EMPTY_LABEL = new String[0];

    @Before
    public void setUp() {
        when(mockCompilationsBean.getTotalCompilationTime()).thenReturn(10000l);
        when(mockCompilationsBean.isCompilationTimeMonitoringSupported()).thenReturn(true);
        collectorUnderTest = new CompilationExports(mockCompilationsBean).register(registry);
    }

    @Test
    public void testCompilationExports() {
        assertEquals(
                10.0,
                registry.getSampleValue(
                        "jvm_compilation_time_seconds_total", EMPTY_LABEL, EMPTY_LABEL),
                .0000001);
    }

    @Test
    public void testCompilationExportsWithFilter() {
        assertEquals(
                10.0,
                registry.getSampleValue(
                        "jvm_compilation_time_seconds_total", EMPTY_LABEL, EMPTY_LABEL, SampleNameFilter.ALLOW_ALL),
                .0000001);
    }

    @Test
    public void testCompilationExportsFiltered() {
        SampleNameFilter sampleNameFilter =
                new SampleNameFilter.Builder()
                        .nameMustNotBeEqualTo("jvm_compilation_time_seconds_total")
                        .build();

        assertNull(
                registry.getSampleValue(
                        "jvm_compilation_time_seconds_total", EMPTY_LABEL, EMPTY_LABEL, sampleNameFilter));
    }
}

package io.prometheus.client.hotspot;

import io.prometheus.metrics.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class VersionInfoExportsTest {

    private CollectorRegistry registry = new CollectorRegistry();

    @Before
    public void setUp() {
        new VersionInfoExports().register(registry);
    }

    @Test
    public void testVersionInfo() {
        assertEquals(
                1L,
                registry.getSampleValue(
                        "jvm_info", new String[]{"runtime", "vendor", "version"},
                        new String[]{
                          System.getProperty("java.runtime.name", "unknown"),
                          System.getProperty("java.vm.vendor", "unknown"),
                          System.getProperty("java.runtime.version", "unknown")}),
                .0000001);
    }
}

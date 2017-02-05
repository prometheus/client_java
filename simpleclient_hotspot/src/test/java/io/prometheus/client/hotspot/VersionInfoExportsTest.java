package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
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
        String UNKNOWN_LABEL_VALUE = "unknown";
        assertEquals(
                1L,
                registry.getSampleValue(
                        "jvm_info", new String[]{"version", "vendor"}, new String[]{System.getProperty("java.runtime.version", UNKNOWN_LABEL_VALUE), System.getProperty("java.vm.vendor", UNKNOWN_LABEL_VALUE)}),
                .0000001);
        assertEquals(
                1L,
                registry.getSampleValue(
                        "os_info", new String[]{"name", "version", "arch"}, new String[]{System.getProperty("os.name", UNKNOWN_LABEL_VALUE), System.getProperty("os.version", UNKNOWN_LABEL_VALUE), System.getProperty("os.arch", UNKNOWN_LABEL_VALUE)}),
                .0000001);
    }
}

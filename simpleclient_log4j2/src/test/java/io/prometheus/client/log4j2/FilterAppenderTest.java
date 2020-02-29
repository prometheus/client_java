package io.prometheus.client.log4j2;

import io.prometheus.client.CollectorRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.prometheus.client.log4j2.InstrumentedAppender.COUNTER_NAME;
import static org.junit.Assert.assertEquals;

public class FilterAppenderTest
{
    private static Logger logger;

    @BeforeClass
    public static void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-filtered-appender-test.xml");
        logger = LogManager.getLogger();
    }

    @Test
    public void testFilteredEvents() {

        // WHEN: some log messages are logged at various levels
        logger.trace("out");
        logger.debug("out");
        logger.info("in");
        logger.warn("in");
        logger.error("in");
        logger.fatal("in");

        // THEN: there are no messages below INFO because they are configured to be filtered out
        assertEquals(0, getLogLevelCount("trace"));
        assertEquals(0, getLogLevelCount("debug"));

        // AND: there are messages at INFO and above
        assertEquals(1, getLogLevelCount("info"));
        assertEquals(1, getLogLevelCount("warn"));
        assertEquals(1, getLogLevelCount("error"));
        assertEquals(1, getLogLevelCount("fatal"));
    }

    private int getLogLevelCount(String level) {
        return CollectorRegistry.defaultRegistry.getSampleValue(COUNTER_NAME,
                new String[]{"level"}, new String[]{level}).intValue();
    }
}

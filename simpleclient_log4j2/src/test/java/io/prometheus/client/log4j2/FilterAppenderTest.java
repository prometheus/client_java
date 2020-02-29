package io.prometheus.client.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.FATAL;
import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.TRACE;
import static org.apache.logging.log4j.Level.WARN;

public class FilterAppenderTest
{
    private static Logger logger;
    private static LogCounter logCounter;

    @BeforeClass
    public static void setUpSpec() {
        System.setProperty("log4j.configurationFile", "log4j2-filtered-appender-test.xml");
        logger = LogManager.getLogger();
        logCounter = new LogCounter();
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
        logCounter.assertLogLevelCountNotIncreased(TRACE);
        logCounter.assertLogLevelCountNotIncreased(DEBUG);

        // AND: there are messages at INFO and above
        logCounter.assertLogLevelCountIncreasedByOne(INFO);
        logCounter.assertLogLevelCountIncreasedByOne(WARN);
        logCounter.assertLogLevelCountIncreasedByOne(ERROR);
        logCounter.assertLogLevelCountIncreasedByOne(FATAL);
    }
}

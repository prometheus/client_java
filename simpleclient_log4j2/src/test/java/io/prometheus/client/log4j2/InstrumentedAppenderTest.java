package io.prometheus.client.log4j2;

import static io.prometheus.client.log4j2.InstrumentedAppender.COUNTER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.client.CollectorRegistry;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Before;
import org.junit.Test;

import static org.apache.logging.log4j.Level.*;

public class InstrumentedAppenderTest {

    private InstrumentedAppender appender;
    private LogEvent event;

    @Before
    public void setUp() throws Exception {
        appender = InstrumentedAppender.createAppender("Prometheus-Appender");
        event = mock(LogEvent.class);
    }

    @Test
    public void metersTraceEvents() throws Exception {
        when(event.getLevel()).thenReturn(TRACE);

        appender.append(event);

        assertEquals(1, getLogLevelCount("trace"));
    }

    @Test
    public void metersDebugEvents() throws Exception {
        when(event.getLevel()).thenReturn(DEBUG);

        appender.append(event);

        assertEquals(1, getLogLevelCount("debug"));
    }

    @Test
    public void metersInfoEvents() throws Exception {
        when(event.getLevel()).thenReturn(INFO);

        appender.append(event);

        assertEquals(1, getLogLevelCount("trace"));
    }

    @Test
    public void metersWarnEvents() throws Exception {
        when(event.getLevel()).thenReturn(WARN);

        appender.append(event);

        assertEquals(1, getLogLevelCount("warn"));
    }

    @Test
    public void metersErrorEvents() throws Exception {
        when(event.getLevel()).thenReturn(ERROR);

        appender.append(event);

        assertEquals(1, getLogLevelCount("error"));
    }

    @Test
    public void metersFatalEvents() throws Exception {
        when(event.getLevel()).thenReturn(FATAL);

        appender.append(event);

        assertEquals(1, getLogLevelCount("fatal"));
    }

    private int getLogLevelCount(String level) {
        return CollectorRegistry.defaultRegistry.getSampleValue(COUNTER_NAME,
                new String[]{"level"}, new String[]{level}).intValue();
    }
}

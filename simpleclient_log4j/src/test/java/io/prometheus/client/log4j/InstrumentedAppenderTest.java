package io.prometheus.client.log4j;

import static io.prometheus.client.log4j.InstrumentedAppender.COUNTER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.client.CollectorRegistry;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

public class InstrumentedAppenderTest {

  private InstrumentedAppender appender;
  private LoggingEvent event;

  @Before
  public void setUp() throws Exception {
    appender = new InstrumentedAppender();
    appender.activateOptions();
    
    event = mock(LoggingEvent.class);
  }

  @Test
  public void metersTraceEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.TRACE);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("trace"));
  }

  @Test
  public void metersDebugEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.DEBUG);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("debug"));
  }

  @Test
  public void metersInfoEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.INFO);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("info"));
  }

  @Test
  public void metersWarnEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.WARN);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("warn"));
  }

  @Test
  public void metersErrorEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.ERROR);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("error"));
  }

  @Test
  public void metersFatalEvents() throws Exception {
    when(event.getLevel()).thenReturn(Level.FATAL);

    appender.doAppend(event);

    assertEquals(1, getLogLevelCount("fatal"));
  }

  private int getLogLevelCount(String level) {
    return CollectorRegistry.defaultRegistry.getSampleValue(COUNTER_NAME, 
            new String[]{"level"}, new String[]{level}).intValue();
  }
}

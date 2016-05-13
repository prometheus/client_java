package io.prometheus.client.logback;

import static io.prometheus.client.logback.InstrumentedAppender.COUNTER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;

public class InstrumentedAppenderTest {

  private InstrumentedAppender appender;
  private ILoggingEvent event;

  @Before
  public void setUp() throws Exception {
    appender = new InstrumentedAppender();
    appender.start();
    
    event = mock(ILoggingEvent.class);
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

  private int getLogLevelCount(String level) {
    return CollectorRegistry.defaultRegistry.getSampleValue(COUNTER_NAME, "level", level).intValue();
  }
}

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
  public abstract static class Base {
    CollectorRegistry registry;
    InstrumentedAppender appender;
    private ILoggingEvent event;

    @Before
    public void setUp() throws Exception {
      this.appender.start();

      this.event = mock(ILoggingEvent.class);
    }

    @Test
    public void metersTraceEvents() throws Exception {
      when(this.event.getLevel()).thenReturn(Level.TRACE);

      this.appender.doAppend(event);

      assertEquals(1, this.getLogLevelCount("trace"));
    }

    @Test
    public void metersDebugEvents() throws Exception {
      when(this.event.getLevel()).thenReturn(Level.DEBUG);

      this.appender.doAppend(event);

      assertEquals(1, this.getLogLevelCount("debug"));
    }

    @Test
    public void metersInfoEvents() throws Exception {
      when(this.event.getLevel()).thenReturn(Level.INFO);

      this.appender.doAppend(event);

      assertEquals(1, this.getLogLevelCount("info"));
    }

    @Test
    public void metersWarnEvents() throws Exception {
      when(this.event.getLevel()).thenReturn(Level.WARN);

      this.appender.doAppend(event);

      assertEquals(1, this.getLogLevelCount("warn"));
    }

    @Test
    public void metersErrorEvents() throws Exception {
      when(this.event.getLevel()).thenReturn(Level.ERROR);

      this.appender.doAppend(event);

      assertEquals(1, this.getLogLevelCount("error"));
    }

    private int getLogLevelCount(String level) {
      return this.registry.getSampleValue(COUNTER_NAME,
              new String[]{"level"}, new String[]{level}).intValue();
    }
  }

  public static class DefaultTest extends Base {
    @Before
    public void setUp() throws Exception {
      this.registry = CollectorRegistry.defaultRegistry;

      this.appender = new InstrumentedAppender();

      super.setUp();
    }
  }

  public static class InstanceTest extends Base {
    @Before
    public void setUp() throws Exception {
      this.registry = new CollectorRegistry();

      this.appender = new InstrumentedAppender(this.registry);

      super.setUp();
    }
  }
}

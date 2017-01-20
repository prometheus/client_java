package io.prometheus.client.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.prometheus.client.Counter;

public class InstrumentedAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  public static final String COUNTER_NAME = "logback_appender_total";
  
  private static final Counter COUNTER;
  private static final Counter.Child TRACE_LABEL;
  private static final Counter.Child DEBUG_LABEL;
  private static final Counter.Child INFO_LABEL;
  private static final Counter.Child WARN_LABEL;
  private static final Counter.Child ERROR_LABEL;
  
  static {
    COUNTER = Counter.build().name(COUNTER_NAME)
            .help("Logback log statements at various log levels")
            .labelNames("level")
            .register();

    TRACE_LABEL = COUNTER.labels("trace");
    DEBUG_LABEL = COUNTER.labels("debug");
    INFO_LABEL = COUNTER.labels("info");
    WARN_LABEL = COUNTER.labels("warn");
    ERROR_LABEL = COUNTER.labels("error");
  }

  /**
   * Create a new instrumented appender using the default registry.
   */
  public InstrumentedAppender() {
  }


  @Override
  public void start() {
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    switch (event.getLevel().toInt()) {
      case Level.TRACE_INT:
        TRACE_LABEL.inc();
        break;
      case Level.DEBUG_INT:
        DEBUG_LABEL.inc();
        break;
      case Level.INFO_INT:
        INFO_LABEL.inc();
        break;
      case Level.WARN_INT:
        WARN_LABEL.inc();
        break;
      case Level.ERROR_INT:
        ERROR_LABEL.inc();
        break;
      default:
        break;
    }
  }
}

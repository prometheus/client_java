package io.prometheus.client.log4j;

import io.prometheus.client.Counter;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class InstrumentedAppender extends AppenderSkeleton {

  public static final String COUNTER_NAME = "log4j_appender_total";

  private static final Counter COUNTER;
  private static final Counter.Child TRACE_LABEL;
  private static final Counter.Child DEBUG_LABEL;
  private static final Counter.Child INFO_LABEL;
  private static final Counter.Child WARN_LABEL;
  private static final Counter.Child ERROR_LABEL;
  private static final Counter.Child FATAL_LABEL;
  
  static {
    COUNTER = Counter.build().name(COUNTER_NAME)
            .help("Log4j log statements at various log levels")
            .labelNames("level")
            .register();

    TRACE_LABEL = COUNTER.labels("trace");
    DEBUG_LABEL = COUNTER.labels("debug");
    INFO_LABEL = COUNTER.labels("info");
    WARN_LABEL = COUNTER.labels("warn");
    ERROR_LABEL = COUNTER.labels("error");
    FATAL_LABEL = COUNTER.labels("fatal");
  }

  /**
   * Create a new instrumented appender using the default registry.
   */
  public InstrumentedAppender() {
  }


  @Override
  public void activateOptions() {
  }

  @Override
  protected void append(LoggingEvent event) {
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
      case Level.FATAL_INT:
        FATAL_LABEL.inc();
        break;
      default:
        break;
    }
  }

  @Override
  public void close() {
    // nothing doing
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }
}

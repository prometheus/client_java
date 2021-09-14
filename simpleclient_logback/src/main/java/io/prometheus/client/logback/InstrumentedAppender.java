package io.prometheus.client.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class InstrumentedAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
  public static final String COUNTER_NAME = "logback_appender_total";
  
  private static final Counter defaultCounter = Counter.build().name(COUNTER_NAME)
          .help("Logback log statements at various log levels")
          .labelNames("level")
          .register();
  private final Counter.Child traceCounter;
  private final Counter.Child debugCounter;
  private final Counter.Child infoCounter;
  private final Counter.Child warnCounter;
  private final Counter.Child errorCounter;

  /**
   * Create a new instrumented appender using the default registry.
   */
  public InstrumentedAppender() {
    this(defaultCounter);
  }

  /**
   * Create a new instrumented appender using the supplied registry.
   */
  public InstrumentedAppender(CollectorRegistry registry) {
    this(Counter.build().name(COUNTER_NAME)
            .help("Logback log statements at various log levels")
            .labelNames("level")
            .register(registry));
  }

  private InstrumentedAppender(Counter counter) {
    this.traceCounter = counter.labels("trace");
    this.debugCounter = counter.labels("debug");
    this.infoCounter = counter.labels("info");
    this.warnCounter = counter.labels("warn");
    this.errorCounter = counter.labels("error");
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    switch (event.getLevel().toInt()) {
      case Level.TRACE_INT:
        this.traceCounter.inc();
        break;
      case Level.DEBUG_INT:
        this.debugCounter.inc();
        break;
      case Level.INFO_INT:
        this.infoCounter.inc();
        break;
      case Level.WARN_INT:
        this.warnCounter.inc();
        break;
      case Level.ERROR_INT:
        this.errorCounter.inc();
        break;
      default:
        break;
    }
  }
}

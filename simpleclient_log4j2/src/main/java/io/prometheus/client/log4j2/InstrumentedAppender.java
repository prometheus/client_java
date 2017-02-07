package io.prometheus.client.log4j2;

import io.prometheus.client.Counter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import static org.apache.logging.log4j.Level.*;

/**
 * Log4j2 log statements at various log levels
 * <p>
 * Example log4j2 configuration:
 * <pre>
 *   &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *   &lt;Configuration packages="io.prometheus.client.log4j2"&gt;
 *     &lt;Appenders&gt;
 *       &lt;Prometheus name="PROMETHEUS"/&gt;
 *     &lt;/Appenders&gt;
 *     &lt;Loggers&gt;
 *       &lt;Root level="trace"&gt;
 *         &lt;AppenderRef ref="PROMETHEUS"/&gt;
 *       &lt;/Root&gt;
 *     &lt;/Loggers&gt;
 *   &lt;/Configuration&gt;
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   log4j_appender_total{level="trace",} 1.0
 *   log4j_appender_total{level="debug",} 2.0
 *   log4j_appender_total{level="info",} 3.0
 *   log4j_appender_total{level="warn",} 4.0
 *   log4j_appender_total{level="error",} 5.0
 *   log4j_appender_total{level="fatal",} 6.0
 * </pre>
 */
@Plugin(name = "Prometheus", category = "Core", elementType = "appender")
public final class InstrumentedAppender extends AbstractAppender {

    public static final String COUNTER_NAME = "log4j2_appender_total";

    private static final Counter COUNTER;
    private static final Counter.Child TRACE_LABEL;
    private static final Counter.Child DEBUG_LABEL;
    private static final Counter.Child INFO_LABEL;
    private static final Counter.Child WARN_LABEL;
    private static final Counter.Child ERROR_LABEL;
    private static final Counter.Child FATAL_LABEL;

    static {
        COUNTER = Counter.build().name(COUNTER_NAME)
                .help("Log4j2 log statements at various log levels")
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
    protected InstrumentedAppender(String name) {
        super(name, null, null);
    }

    @Override
    public void append(LogEvent event) {
        Level level = event.getLevel();
        if (TRACE.equals(level)) TRACE_LABEL.inc();
        else if (DEBUG.equals(level)) DEBUG_LABEL.inc();
        else if (INFO.equals(level)) INFO_LABEL.inc();
        else if (WARN.equals(level)) WARN_LABEL.inc();
        else if (ERROR.equals(level)) ERROR_LABEL.inc();
        else if (FATAL.equals(level)) FATAL_LABEL.inc();
    }

    @PluginFactory
    public static InstrumentedAppender createAppender(
            @PluginAttribute("name") String name) {
        if (name == null) {
            LOGGER.error("No name provided for InstrumentedAppender");
            return null;
        }
        return new InstrumentedAppender(name);
    }

}

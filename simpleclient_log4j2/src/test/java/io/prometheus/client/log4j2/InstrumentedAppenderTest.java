package io.prometheus.client.log4j2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.logging.log4j.Level.*;

@RunWith(Parameterized.class)
public class InstrumentedAppenderTest {

    private final Level level;
    private LogCounter logCounter;
    private InstrumentedAppender appender;
    private LogEvent event;

    @Parameterized.Parameters(name = "{index}: level = {0}")
    public static Collection<Level> data() {
        return Arrays.asList(TRACE, DEBUG, INFO, WARN, ERROR, FATAL);
    }

    public InstrumentedAppenderTest(Level level)
    {
        this.level = level;
        appender = InstrumentedAppender.createAppender("Prometheus-Appender",
                true, null, null);
        event = mock(LogEvent.class);
        logCounter = new LogCounter();
    }

    @Test
    public void shouldCountLogLevelCorrectly() {
        when(event.getLevel()).thenReturn(level);

        appender.append(event);

        logCounter.assertLogLevelCountIncreasedByOne(level);
    }
}

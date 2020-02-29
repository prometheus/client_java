package io.prometheus.client.log4j2;

import io.prometheus.client.CollectorRegistry;
import org.apache.logging.log4j.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static io.prometheus.client.log4j2.InstrumentedAppender.COUNTER_NAME;
import static org.junit.Assert.assertEquals;

final class LogCounter
{
    Map<String, Integer> snapshot = getSnapshot();

    void assertLogLevelCountIncreasedByOne(Level level) {
        int[] result = getExpectedAndActual(level);

        assertEquals(result[0], result[1]);
    }

    void assertLogLevelCountNotIncreased(Level level) {
        int[] result = getExpectedAndActual(level, 0);

        assertEquals(result[0], result[1]);
    }

    private int[] getExpectedAndActual(Level level) {
        return getExpectedAndActual(level, 1);
    }

    private int[] getExpectedAndActual(Level level, int increase) {
        int[] result = new int[2];
        String l = level.name().toLowerCase(Locale.US);

        result[0] = snapshot.get(l) + increase;
        result[1] = getLogLevelCount(l);

        return result;
    }

    private static int getLogLevelCount(String level) {
        return CollectorRegistry.defaultRegistry.getSampleValue(COUNTER_NAME,
                new String[]{"level"}, new String[]{level}).intValue();
    }

    private static Map<String, Integer> getSnapshot() {
        Map<String, Integer> map = new HashMap<String, Integer>(6);

        map.put("trace", getLogLevelCount("trace"));
        map.put("debug", getLogLevelCount("debug"));
        map.put("info", getLogLevelCount("info"));
        map.put("warn", getLogLevelCount("warn"));
        map.put("error", getLogLevelCount("error"));
        map.put("fatal", getLogLevelCount("fatal"));

        return Collections.unmodifiableMap(map);
    }
}

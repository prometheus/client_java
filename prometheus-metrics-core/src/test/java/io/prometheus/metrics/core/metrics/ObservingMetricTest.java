package io.prometheus.metrics.core.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

public class ObservingMetricTest {

    @Test
    public void testLabelRemoveWhileCollecting() throws Exception {
        Counter counter = Counter.newBuilder().withName("test").withLabelNames("label1", "label2").build();
        Field data = counter.getClass().getSuperclass().getDeclaredField("data");
        data.setAccessible(true);

        counter.withLabelValues("a", "b").inc(1.0);
        counter.withLabelValues("c", "d").inc(3.0);
        counter.withLabelValues("e", "f").inc(7.0);

        // collect() iterates over data.entrySet().
        // remove() removes entries from data.
        // Make sure iterating does not yield null while removing.

        int i = 0;
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) data.get(counter)).entrySet()) {
            i++;
            if (i == 2) {
                counter.remove("c", "d");
                counter.remove("e", "f");
            }
            Assert.assertNotNull(entry.getKey());
            Assert.assertNotNull(entry.getValue());
        }
    }
}

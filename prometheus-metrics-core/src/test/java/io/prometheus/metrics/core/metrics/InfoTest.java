package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import org.junit.Assert;
import org.junit.Test;

public class InfoTest {

    @Test
    public void testIncrement() {
        Info info = Info.newBuilder().withName("target_info").build();
        info.info(Labels.of("key", "value"));
        InfoSnapshot snapshot = info.collect();
        Assert.assertEquals("target", snapshot.getMetadata().getName());
        Assert.assertEquals(1, snapshot.getData().size());
        InfoSnapshot.InfoData data = snapshot.getData().stream().findAny().orElseThrow(RuntimeException::new);
        Assert.assertEquals(Labels.of("key", "value"), data.getLabels());
    }
}

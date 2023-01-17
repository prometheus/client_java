package io.prometheus.metrics.model;

import org.junit.Test;

public class CounterSnapshotTest {

    @Test
    public void testCompleteBuilderGoodCase() {
        CounterSnapshot snapshot = CounterSnapshot.newBuilder()
                .withName("my_counter")
                .withHelp("some help")
                .withUnit(new Unit("requests"))
                .addCounterData(CounterSnapshot.CounterData.newBuilder()
                        .withValue(1.0)
                        .withExemplar(Exemplar.newBuilder()
                                .withValue(3.0)
                                .withTraceId("abc123")
                                .withSpanId("123457")
                                .withTimestampMillis(System.currentTimeMillis())
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("path", "/hello")
                                .build())
                        .withCreatedTimestampMillis(System.currentTimeMillis())
                        .build()
                )
                .build();
    }
}

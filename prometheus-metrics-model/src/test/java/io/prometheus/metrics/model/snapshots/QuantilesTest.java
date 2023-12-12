package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantilesTest {

    @Test
    void testSort() {
        Quantiles quantiles = Quantiles.builder()
                .quantile(0.99, 0.23)
                .quantile(0.5, 0.2)
                .quantile(0.95, 0.22)
                .build();
        Assertions.assertEquals(3, quantiles.size());
        Assertions.assertEquals(0.5, quantiles.get(0).getQuantile(), 0);
        Assertions.assertEquals(0.2, quantiles.get(0).getValue(), 0);
        Assertions.assertEquals(0.95, quantiles.get(1).getQuantile(), 0);
        Assertions.assertEquals(0.22, quantiles.get(1).getValue(), 0);
        Assertions.assertEquals(0.99, quantiles.get(2).getQuantile(), 0);
        Assertions.assertEquals(0.23, quantiles.get(2).getValue(), 0);
    }

    @Test
    void testImmutable() {
        Quantiles quantiles = Quantiles.builder()
                .quantile(0.99, 0.23)
                .quantile(0.5, 0.2)
                .quantile(0.95, 0.22)
                .build();
        Iterator<Quantile> iterator = quantiles.iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testEmpty() {
        Assertions.assertEquals(0, Quantiles.EMPTY.size());
    }

    @Test
    void testDuplicate() {
        assertThrows(IllegalArgumentException.class,
                () ->
                Quantiles.builder()
                        .quantile(0.95, 0.23)
                        .quantile(0.5, 0.2)
                        .quantile(0.95, 0.22)
                        .build());
    }
}

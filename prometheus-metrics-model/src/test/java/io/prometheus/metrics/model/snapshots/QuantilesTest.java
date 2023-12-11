package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class QuantilesTest {

    @Test
    public void testSort() {
        Quantiles quantiles = Quantiles.builder()
                .quantile(0.99, 0.23)
                .quantile(0.5, 0.2)
                .quantile(0.95, 0.22)
                .build();
        Assert.assertEquals(3, quantiles.size());
        Assert.assertEquals(0.5, quantiles.get(0).getQuantile(), 0);
        Assert.assertEquals(0.2, quantiles.get(0).getValue(), 0);
        Assert.assertEquals(0.95, quantiles.get(1).getQuantile(), 0);
        Assert.assertEquals(0.22, quantiles.get(1).getValue(), 0);
        Assert.assertEquals(0.99, quantiles.get(2).getQuantile(), 0);
        Assert.assertEquals(0.23, quantiles.get(2).getValue(), 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        Quantiles quantiles = Quantiles.builder()
                .quantile(0.99, 0.23)
                .quantile(0.5, 0.2)
                .quantile(0.95, 0.22)
                .build();
        Iterator<Quantile> iterator = quantiles.iterator();
        iterator.next();
        iterator.remove();
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals(0, Quantiles.EMPTY.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicate() {
        Quantiles.builder()
                .quantile(0.95, 0.23)
                .quantile(0.5, 0.2)
                .quantile(0.95, 0.22)
                .build();
    }
}

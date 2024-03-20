package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ExemplarsTest {

    @Test
    public void testUpperBound() {
        Exemplars exemplars = Exemplars.of(
                Exemplar.builder().value(1.0).build(),
                Exemplar.builder().value(3.0).build(),
                Exemplar.builder().value(2.0).build()
        );
        Assert.assertEquals(3, exemplars.size());
        Assert.assertEquals(1.0, exemplars.get(0).getValue(), 0.0);
        Assert.assertEquals(3.0, exemplars.get(1).getValue(), 0.0);
        Assert.assertEquals(2.0, exemplars.get(2).getValue(), 0.0);
        Assert.assertEquals(1.0, exemplars.get(0.0, Double.POSITIVE_INFINITY).getValue(), 0.0);
        Assert.assertEquals(1.0, exemplars.get(0.0, 1.0).getValue(), 0.0);
        Assert.assertEquals(3.0, exemplars.get(1.0, 4.0).getValue(), 0.0);
        Assert.assertEquals(3.0, exemplars.get(2.0, 3.0).getValue(), 0.0);
        Assert.assertEquals(2.0, exemplars.get(1.0, 2.1).getValue(), 0.0);
        Assert.assertNull(exemplars.get(2.0, 2.1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        Exemplars exemplars = Exemplars.of(
                Exemplar.builder().value(1.0).build(),
                Exemplar.builder().value(3.0).build(),
                Exemplar.builder().value(2.0).build()
        );
        Iterator<Exemplar> iterator = exemplars.iterator();
        iterator.next();
        iterator.remove();
    }

    @Test
    public void testGet() {
        Exemplar oldest = Exemplar.builder().timestampMillis(System.currentTimeMillis() - 100).value(1.8).build();
        Exemplar middle =  Exemplar.builder().timestampMillis(System.currentTimeMillis() - 50).value(1.2).build();
        Exemplar newest = Exemplar.builder().timestampMillis(System.currentTimeMillis()).value(1.0).build();
        Exemplars exemplars = Exemplars.of(oldest, newest, middle);
        Exemplar result = exemplars.get(1.1, 1.9); // newest is not within these bounds
        Assert.assertSame(result, middle);
        result = exemplars.get(0.9, Double.POSITIVE_INFINITY);
        Assert.assertSame(result, newest);
    }
}

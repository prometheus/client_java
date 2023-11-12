package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class UnitTest {

    @Test
    public void testEmpty() {
        try {
            new Unit(" ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEquals1() {
        Unit unit1 = Unit.BYTES;
        Unit unit2 = new Unit("bytes");

        Assert.assertEquals(unit2, unit1);
    }

    @Test
    public void testEquals2() {
        Unit unit1 = new Unit("bytes ");
        Unit unit2 = new Unit("bytes");

        Assert.assertEquals(unit2, unit1);
    }

    @Test
    public void testEquals3() {
        Unit unit1 = new Unit(" bytes");
        Unit unit2 = new Unit("bytes");

        Assert.assertEquals(unit2, unit1);
    }

    @Test
    public void testEquals4() {
        Unit unit1 = new Unit(" bytes ");
        Unit unit2 = new Unit("bytes");

        Assert.assertEquals(unit2, unit1);
    }
}

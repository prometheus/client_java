package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


class UnitTest {

    @Test
    void testEmpty() {
        try {
            new Unit(" ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    void testEquals1() {
        Unit unit1 = Unit.BYTES;
        Unit unit2 = new Unit("bytes");

        Assertions.assertEquals(unit2, unit1);
    }

    @Test
    void testEquals2() {
        Unit unit1 = new Unit("bytes ");
        Unit unit2 = new Unit("bytes");

        Assertions.assertEquals(unit2, unit1);
    }

    @Test
    void testEquals3() {
        Unit unit1 = new Unit(" bytes");
        Unit unit2 = new Unit("bytes");

        Assertions.assertEquals(unit2, unit1);
    }

    @Test
    void testEquals4() {
        Unit unit1 = new Unit(" bytes ");
        Unit unit2 = new Unit("bytes");

        Assertions.assertEquals(unit2, unit1);
    }
}

package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

class ExemplarsTest {

    @Test
    void testUpperBound() {
        Exemplars exemplars = Exemplars.of(
                Exemplar.builder().value(1.0).build(),
                Exemplar.builder().value(3.0).build(),
                Exemplar.builder().value(2.0).build()
        );
        Assertions.assertEquals(3, exemplars.size());
        Assertions.assertEquals(1.0, exemplars.get(0).getValue(), 0.0);
        Assertions.assertEquals(3.0, exemplars.get(1).getValue(), 0.0);
        Assertions.assertEquals(2.0, exemplars.get(2).getValue(), 0.0);
        Assertions.assertEquals(1.0, exemplars.get(0.0, Double.POSITIVE_INFINITY).getValue(), 0.0);
        Assertions.assertEquals(1.0, exemplars.get(0.0, 1.0).getValue(), 0.0);
        Assertions.assertEquals(3.0, exemplars.get(1.0, 4.0).getValue(), 0.0);
        Assertions.assertEquals(3.0, exemplars.get(2.0, 3.0).getValue(), 0.0);
        Assertions.assertEquals(2.0, exemplars.get(1.0, 2.1).getValue(), 0.0);
        Assertions.assertNull(exemplars.get(2.0, 2.1));
    }

    @Test
    void testImmutable() {
        Exemplars exemplars = Exemplars.of(
                Exemplar.builder().value(1.0).build(),
                Exemplar.builder().value(3.0).build(),
                Exemplar.builder().value(2.0).build()
        );
        Iterator<Exemplar> iterator = exemplars.iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}

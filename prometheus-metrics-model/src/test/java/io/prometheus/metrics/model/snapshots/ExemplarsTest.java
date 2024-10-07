package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class ExemplarsTest {

  @Test
  public void testUpperBound() {
    Exemplars exemplars =
        Exemplars.of(
            Exemplar.builder().value(1.0).build(),
            Exemplar.builder().value(3.0).build(),
            Exemplar.builder().value(2.0).build());
    assertThat(exemplars.size()).isEqualTo(3);
    assertThat(exemplars.get(0).getValue()).isEqualTo(1.0);
    assertThat(exemplars.get(1).getValue()).isEqualTo(3.0);
    assertThat(exemplars.get(2).getValue()).isEqualTo(2.0);
    assertThat(exemplars.get(0.0, Double.POSITIVE_INFINITY).getValue()).isEqualTo(1.0);
    assertThat(exemplars.get(0.0, 1.0).getValue()).isEqualTo(1.0);
    assertThat(exemplars.get(1.0, 4.0).getValue()).isEqualTo(3.0);
    assertThat(exemplars.get(2.0, 3.0).getValue()).isEqualTo(3.0);
    assertThat(exemplars.get(1.0, 2.1).getValue()).isEqualTo(2.0);
    assertThat(exemplars.get(2.0, 2.1)).isNull();
  }

  @Test
  public void testImmutable() {
    Exemplars exemplars =
        Exemplars.of(
            Exemplar.builder().value(1.0).build(),
            Exemplar.builder().value(3.0).build(),
            Exemplar.builder().value(2.0).build());
    Iterator<Exemplar> iterator = exemplars.iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  public void testGet() {
    Exemplar oldest =
        Exemplar.builder().timestampMillis(System.currentTimeMillis() - 100).value(1.8).build();
    Exemplar middle =
        Exemplar.builder().timestampMillis(System.currentTimeMillis() - 50).value(1.2).build();
    Exemplar newest =
        Exemplar.builder().timestampMillis(System.currentTimeMillis()).value(1.0).build();
    Exemplars exemplars = Exemplars.of(oldest, newest, middle);
    Exemplar result = exemplars.get(1.1, 1.9); // newest is not within these bounds
    assertThat(middle).isSameAs(result);
    result = exemplars.get(0.9, Double.POSITIVE_INFINITY);
    assertThat(newest).isSameAs(result);
  }
}

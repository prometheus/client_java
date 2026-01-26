package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class QuantilesTest {

  @Test
  void testSort() {
    Quantiles quantiles =
        Quantiles.builder().quantile(0.99, 0.23).quantile(0.5, 0.2).quantile(0.95, 0.22).build();
    assertThat(quantiles.size()).isEqualTo(3);
    assertThat(quantiles.get(0).getQuantile()).isEqualTo(0.5);
    assertThat(quantiles.get(0).getValue()).isEqualTo(0.2);
    assertThat(quantiles.get(1).getQuantile()).isEqualTo(0.95);
    assertThat(quantiles.get(1).getValue()).isEqualTo(0.22);
    assertThat(quantiles.get(2).getQuantile()).isEqualTo(0.99);
    assertThat(quantiles.get(2).getValue()).isEqualTo(0.23);
  }

  @Test
  void testImmutable() {
    Quantiles quantiles =
        Quantiles.builder().quantile(0.99, 0.23).quantile(0.5, 0.2).quantile(0.95, 0.22).build();
    Iterator<Quantile> iterator = quantiles.iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  void testEmpty() {
    assertThat(Quantiles.EMPTY.size()).isZero();
  }

  @Test
  void testDuplicate() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                Quantiles.builder()
                    .quantile(0.95, 0.23)
                    .quantile(0.5, 0.2)
                    .quantile(0.95, 0.22)
                    .build());
  }
}

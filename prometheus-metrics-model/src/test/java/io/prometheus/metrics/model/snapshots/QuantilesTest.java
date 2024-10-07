package io.prometheus.metrics.model.snapshots;

import java.util.Iterator;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class QuantilesTest {

  @Test
  public void testSort() {
    Quantiles quantiles =
        Quantiles.builder().quantile(0.99, 0.23).quantile(0.5, 0.2).quantile(0.95, 0.22).build();
    assertThat(quantiles.size()).isEqualTo(3);
    assertThat(quantiles.get(0).getQuantile()).isCloseTo(0.5, offset(0.0));
    assertThat(quantiles.get(0).getValue()).isCloseTo(0.2, offset(0.0));
    assertThat(quantiles.get(1).getQuantile()).isCloseTo(0.95, offset(0.0));
    assertThat(quantiles.get(1).getValue()).isCloseTo(0.22, offset(0.0));
    assertThat(quantiles.get(2).getQuantile()).isCloseTo(0.99, offset(0.0));
    assertThat(quantiles.get(2).getValue()).isCloseTo(0.23, offset(0.0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testImmutable() {
    Quantiles quantiles =
        Quantiles.builder().quantile(0.99, 0.23).quantile(0.5, 0.2).quantile(0.95, 0.22).build();
    Iterator<Quantile> iterator = quantiles.iterator();
    iterator.next();
    iterator.remove();
  }

  @Test
  public void testEmpty() {
    assertThat(Quantiles.EMPTY.size()).isZero();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicate() {
    Quantiles.builder().quantile(0.95, 0.23).quantile(0.5, 0.2).quantile(0.95, 0.22).build();
  }
}

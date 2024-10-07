package io.prometheus.metrics.model.snapshots;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitTest {

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new Unit(" ");
  }

  @Test
  public void testEquals1() {
    Unit unit1 = Unit.BYTES;
    Unit unit2 = new Unit("bytes");

    assertThat(unit1).isEqualTo(unit2);
  }

  @Test
  public void testEquals2() {
    Unit unit1 = new Unit("bytes ");
    Unit unit2 = new Unit("bytes");

    assertThat(unit1).isEqualTo(unit2);
  }

  @Test
  public void testEquals3() {
    Unit unit1 = new Unit(" bytes");
    Unit unit2 = new Unit("bytes");

    assertThat(unit1).isEqualTo(unit2);
  }

  @Test
  public void testEquals4() {
    Unit unit1 = new Unit(" bytes ");
    Unit unit2 = new Unit("bytes");

    assertThat(unit1).isEqualTo(unit2);
  }
}

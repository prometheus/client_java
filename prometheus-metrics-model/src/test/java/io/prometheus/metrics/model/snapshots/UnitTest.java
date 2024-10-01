package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

public class UnitTest {

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new Unit(" ");
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

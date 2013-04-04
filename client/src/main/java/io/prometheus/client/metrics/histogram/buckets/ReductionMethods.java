/*
 * Copyright 2013 Prometheus Team Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.prometheus.client.metrics.histogram.buckets;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class ReductionMethods {
  public static ReductionMethod minimum = new ReductionMethod() {
    @Override
    public double reduce(final List<Double> values) {
      final Min minimum = new Min();
      for (final Double value : values) {
        minimum.increment(value);
      }

      return (double) minimum.getResult();
    }
  };

  public static ReductionMethod maximum = new ReductionMethod() {
    @Override
    public double reduce(final List<Double> values) {
      final Max maximum = new Max();
      for (final Double value : values) {
        maximum.increment(value);
      }

      return (double) maximum.getResult();
    }
  };

  public static ReductionMethod average = new ReductionMethod() {
    @Override
    public double reduce(final List<Double> values) {
      final Mean mean = new Mean();
      for (final Double value : values) {
        mean.increment(value);
      }

      return (float) mean.getResult();
    }
  };

  public static ReductionMethod median = new ReductionMethod() {
    @Override
    public double reduce(final List<Double> values) {
      final Median median = new Median();
      final double[] asArray = new double[values.size()];
      for (int i = 0; i < values.size(); i++) {
        asArray[i] = values.get(i);
      }
      return (float) median.evaluate(asArray);
    }
  };
}

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

import com.google.common.base.Function;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import javax.annotation.Nullable;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class TallyingBucketEstimators {
  private static Function<TallyingBucket.Estimator, TallyingBucket.Estimator> emptyFilter =
      new Function<TallyingBucket.Estimator, TallyingBucket.Estimator>() {
        @Override
        public TallyingBucket.Estimator apply(final @Nullable TallyingBucket.Estimator input) {
          return new TallyingBucket.Estimator() {
            @Override
            public double estimateFor(final double minimum, final double maximum, final int index,
                final int observations) {
              if (observations == 0) {
                return Double.NaN;
              } else {
                return input.estimateFor(maximum, maximum, index, observations);
              }
            }
          };
        }
      };

  public static TallyingBucket.Estimator minimum = emptyFilter
      .apply(new TallyingBucket.Estimator() {
        @Override
        public double estimateFor(final double minimum, final double maximum, final int index,
            final int observations) {
          return minimum;
        }
      });

  public static TallyingBucket.Estimator maximum = emptyFilter
      .apply(new TallyingBucket.Estimator() {
        @Override
        public double estimateFor(final double minimum, final double maximum, final int index,
            final int observations) {
          return maximum;
        }
      });

  public static TallyingBucket.Estimator average = emptyFilter
      .apply(new TallyingBucket.Estimator() {
        // XXX: Rename averageOfExtrema
        @Override
        public double estimateFor(final double minimum, final double maximum, final int index,
            final int observations) {
          return (float) (new Mean().evaluate(new double[] {minimum, maximum}));
        }
      });

  public static TallyingBucket.Estimator uniform = emptyFilter
      .apply(new TallyingBucket.Estimator() {
        // XXX: Rename regional.
        private float lowerThird = 1f / 3f;
        private float upperThird = 2f / 3f;

        @Override
        public double estimateFor(final double minimum, final double maximum, final int index,
            final int observations) {
          if (observations == 1) {
            return minimum;
          }

          final float region = (float) index / (float) observations;

          if (region > upperThird) {
            return maximum;
          } else if (region < lowerThird) {
            return maximum;
          }

          return (float) (new Mean().evaluate(new double[] {minimum, maximum}));

        }
      });

  // XXX: Implement linear.

}

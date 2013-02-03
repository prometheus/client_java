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

import com.google.common.collect.ImmutableList;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Distributions {
  public static List<Float> equallySizedBucketsFor(final float lower, final float upper,
      final int count) {
    final ImmutableList.Builder<Float> builder = ImmutableList.builder();

    final float partitionSize = (upper - lower) / count;

    for (int i = 0; i < count; i++) {
      builder.add(lower + ((float) i * partitionSize));
    }

    return builder.build();
  }

  public static List<Float> logarithmicSizedBucketsFor(final float lower, final float upper) {
    final ImmutableList.Builder<Float> builder = ImmutableList.builder();
    final int bucketCount = (int) Math.ceil(Math.log(upper) / Math.log(2));

    for (int i = 0; i < bucketCount; i++) {
      final float j = (float) Math.pow(2, i + 1);
      builder.add(j);
    }

    return builder.build();
  }
}

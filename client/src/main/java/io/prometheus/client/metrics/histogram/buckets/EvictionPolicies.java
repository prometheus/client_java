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

import java.util.ArrayList;
import java.util.List;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class EvictionPolicies {
  public static AccumulatingBucket.EvictionPolicy evictOldest(final int count) {
    return new AccumulatingBucket.EvictionPolicy() {
      @Override
      public void evict(final List<Float> values) {
        final int valueCount = values.size();

        if (count > valueCount) {
          // XXX
          return;
        }

        for (int i = valueCount - 1; i >= valueCount - count; i--) {
          values.remove(i);
        }
      }
    };
  }

  public static AccumulatingBucket.EvictionPolicy evictAndReplaceWith(final int count,
      final ReductionMethod reducer) {
    return new AccumulatingBucket.EvictionPolicy() {
      @Override
      public void evict(final List<Float> values) {
        final int valueCount = values.size();

        if (count > valueCount) {
          // XXX
          return;
        }

        final ArrayList pending = new ArrayList(valueCount);
        for (int i = valueCount - 1; i >= valueCount - count; i--) {
          pending.add(values.remove(i));
        }

        values.add(reducer.reduce(pending));
      }
    };
  }
}

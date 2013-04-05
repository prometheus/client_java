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

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;
import io.prometheus.client.metrics.histogram.Bucket;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class TallyingBucket implements Bucket {
  // XXX: Purposely implemented without locking.

  private final AtomicDouble largestObserved = new AtomicDouble(Float.MIN_VALUE);
  private final AtomicDouble smallestObserved = new AtomicDouble(Float.MAX_VALUE);
  private final AtomicInteger observations = new AtomicInteger(0);

  private final Estimator estimator;

  public TallyingBucket(final Estimator estimator) {
    this.estimator = estimator;
  }

  @Override
  public void add(final double value) {
    observations.incrementAndGet();

    if (largestObserved.get() < value) {
      largestObserved.getAndSet(value);
    }

    if (smallestObserved.get() > value) {
      smallestObserved.getAndSet(value);
    }
  }

  @Override
  public int observations() {
    return observations.get();
  }

  @Override
  public void reset() {
    smallestObserved.getAndSet(Float.MAX_VALUE);
    largestObserved.getAndSet(Float.MAX_VALUE);
    observations.getAndSet(0);
  }

  @Override
  public double valueForIndex(final int index) {
    return estimator.estimateFor(smallestObserved.get(), largestObserved.get(), index,
        observations.get());
  }

  public static interface Estimator {
    double estimateFor(final double minimum, final double maximum, final int index,
        final int observations);
  }


  public static class BucketBuilder implements io.prometheus.client.metrics.histogram.BucketBuilder {
    private final Estimator estimator;

    public BucketBuilder(final Estimator estimator) {
      this.estimator = estimator;
    }

    @Override
    public Bucket newBucket() {
      return new TallyingBucket(estimator);
    }
  }
}

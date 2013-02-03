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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.prometheus.client.metrics.histogram.Bucket;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class AccumulatingBucket implements Bucket {
  private final EvictionPolicy evictionPolicy;
  private final AtomicInteger observations = new AtomicInteger(0);
  private final int capacity;
  private final List<Float> samples;

  public AccumulatingBucket(final EvictionPolicy evictionPolicy, final int capacity) {
    this.evictionPolicy = evictionPolicy;
    this.capacity = capacity;

    samples = new ArrayList<Float>(capacity);
  }

  @Override
  public synchronized void add(final float value) {
    observations.incrementAndGet();

    final int size = samples.size() + 1;

    if (size == capacity) {
      evictionPolicy.evict(samples);
    }

    samples.add(value);
  }

  @Override
  public synchronized int observations() {
    return observations.get();
  }

  @Override
  public synchronized void reset() {
    samples.clear();
  }

  @Override
  public synchronized float valueForIndex(final int index) {
    final int sampleCount = samples.size();

    if (sampleCount == 0) {
      return Float.NaN;
    }

    final ArrayList<Float> temporary = new ArrayList<Float>(samples);
    Collections.sort(temporary);

    final int targetIndex =
        (int) ((float) (sampleCount - 1) * ((float) index / (float) observations.get()));

    return temporary.get(targetIndex);
  }

  public static interface EvictionPolicy {
    public void evict(final List<Float> values);
  }

  public static class BucketBuilder implements io.prometheus.client.metrics.histogram.BucketBuilder {
    private final EvictionPolicy evictionPolicy;
    private final int capacity;

    public BucketBuilder(final EvictionPolicy evictionPolicy, final int capacity) {
      this.evictionPolicy = evictionPolicy;
      this.capacity = capacity;
    }

    @Override
    public Bucket newBucket() {
      return new AccumulatingBucket(evictionPolicy, capacity);
    }
  }
}

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

package io.prometheus.client.metrics;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

import io.prometheus.client.metrics.histogram.Bucket;
import io.prometheus.client.metrics.histogram.BucketBuilder;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Histogram extends StatefulGenerator<Histogram.Vector> {
  private final float[] reportablePercentiles;
  private final List<Float> bucketStarts;
  private final BucketBuilder bucketBuilder;

  public Histogram(final float[] reportablePercentiles, final List<Float> bucketStarts,
      final BucketBuilder bucketBuilder) {
    this.reportablePercentiles = reportablePercentiles;
    this.bucketStarts = bucketStarts;
    this.bucketBuilder = bucketBuilder;
  }

  public void add(final Map<String, String> labels, final float sample) {
    final Vector vector = forLabels(labels);

    vector.add(sample);
  }

  public static class Serializer implements JsonSerializer<Histogram> {
    @Override
    public JsonElement serialize(final Histogram src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();
      final JsonArray value = new JsonArray();
      for (final Map<String, String> labelSet : src.vectors.keySet()) {
        final JsonObject element = new JsonObject();
        element.add("labels", context.serialize(labelSet));
        final Vector vector = src.vectors.get(labelSet);
        final JsonObject values = new JsonObject();
        for (final float percentile : vector.reportablePercentiles) {
          values.add(Float.valueOf(percentile).toString(),
              context.serialize(Float.valueOf(vector.percentile(percentile)).toString()));
        }
        element.add("value", values);
        value.add(element);
      }
      container.add("value", value);
      container.addProperty("type", "histogram");

      return container;
    }
  }

  @Override
  protected Vector newVector() {
    final ImmutableList.Builder<Bucket> builder = ImmutableList.builder();

    for (final Float start : bucketStarts) {
      builder.add(bucketBuilder.newBucket());
    }

    return new Vector(builder.build(), reportablePercentiles);
  }


  protected class Vector implements StatefulGenerator.Vector<Vector> {
    final List<Bucket> buckets;
    // XXX: Law of Demeter.
    final float[] reportablePercentiles;

    private Vector(final List<Bucket> buckets, final float[] reportablePercentiles) {
      this.buckets = buckets;
      this.reportablePercentiles = reportablePercentiles;
    }

    private synchronized void add(final float sample) {
      int lastIndex = 0;
      for (int i = 0; i < bucketStarts.size(); i++) {
        if (sample < bucketStarts.get(i)) {
          break;
        }
        lastIndex = i;
      }
      buckets.get(lastIndex).add(sample);
    }

    private int previousCumulativeObservations(final List<Integer> observations,
        final int bucketIndex) {
      if (bucketIndex == 0) {
        return 0;
      }

      return observations.get(bucketIndex - 1);
    }

    private int prospectiveIndexForPercentile(final float percentile, final int totalObservations) {
      return (int) (percentile * (float) (totalObservations - 1));
    }

    private Bucket nextNonEmptyBucketElement(final int currentIndex, final int bucketcount,
        final List<Integer> observations) {
      for (int i = currentIndex; i < bucketcount; i++) {
        if (observations.get(i) == 0) {
          continue;
        }

        return buckets.get(i);
      }

      throw new IllegalStateException("unreachable");
    }

    private class Offset {
      final Bucket bucket;
      final int index;

      private Offset(final Bucket bucket, final int index) {
        this.bucket = bucket;
        this.index = index;
      }
    }

    private Offset bucketForPercentile(final float percentile) {
      final ImmutableList.Builder<Integer> observationsByBucketBuilder = ImmutableList.builder();
      final ImmutableList.Builder<Integer> cumulativeObservationsByBucketBuilder =
          ImmutableList.builder();
      int totalObservations = 0;

      for (int i = 0; i < bucketStarts.size(); i++) {
        final Bucket bucket = buckets.get(i);
        final int observations = bucket.observations();
        observationsByBucketBuilder.add(observations);
        totalObservations += observations;
        cumulativeObservationsByBucketBuilder.add(totalObservations);
      }

      final int prospectiveIndex = prospectiveIndexForPercentile(percentile, totalObservations);

      final List<Integer> observationsByBucket = observationsByBucketBuilder.build();
      final List<Integer> cumulativeObservationsByBucket =
          cumulativeObservationsByBucketBuilder.build();

      for (int i = 0; i < cumulativeObservationsByBucket.size(); i++) {
        final int cumulativeObservation = cumulativeObservationsByBucket.get(i);

        if (cumulativeObservation == 0) {
          continue;
        }

        if (cumulativeObservation >= prospectiveIndex) {
          final int subIndex =
              prospectiveIndex - previousCumulativeObservations(cumulativeObservationsByBucket, i);

          if (observationsByBucket.get(i) == subIndex) {

            return new Offset(
                nextNonEmptyBucketElement(i + 1, buckets.size(), observationsByBucket), 0);

          }

          return new Offset(buckets.get(i), subIndex);
        }
      }

      return new Offset(buckets.get(0), 0);
    }

    private float percentile(final float percentile) {
      final Offset offset = bucketForPercentile(percentile);
      return offset.bucket.valueForIndex(offset.index);
    }

    @Override
    public Vector value() {
      return this;
    }

    @Override
    public synchronized void reset() {
      for (final Bucket bucket : buckets) {
        bucket.reset();
      }
    }
  }
}

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

import java.util.Map;

import com.google.common.collect.MapMaker;

/**
 * <p>
 * {@link StatefulGenerator} provides a simple means for label-oriented metric
 * exposition in cases whereby the generation of new dimensions entails no
 * prerequisite state knowledge of the given {@link Metric}.
 * </p>
 * 
 * @param <V> The {@link StatefulGenerator.Vector} type name that is responsible
 *        for representing a given dimension.
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
abstract class StatefulGenerator<V extends StatefulGenerator.Vector> implements Metric {
  protected final Map<Map<String, String>, V> vectors = new MapMaker().makeMap();

  @Override
  public void ResetAll() {
    vectors.clear();
  }

  /**
   * <p>
   * Get a {@link V} Vector for this given label set.
   * </p>
   * 
   * <p>
   * If the label set has never been seen, a new dimensional vector shall be
   * furnished.
   * </p>
   * 
   * @param labels The labels against which the query is performed.
   * @return The new {@link V} Vector.
   */
  protected V forLabels(final Map<String, String> labels) {
    // XXX: ImmutableMap for freezing.
    if (!vectors.containsKey(labels)) {
      vectors.put(labels, newVector());
    }

    return vectors.get(labels);
  }

  /**
   * <p>
   * Build a new {@link V} Vector given the requirements of this fundamental
   * type.
   * </p>
   * 
   * @return
   */
  protected abstract V newVector();

  /**
   * <p>
   * The {@link Vector} is a model of a given dimension of the {@link Metric},
   * given the label-orientation.
   * </p>
   * 
   * @param <T> The fundamental value type of the dimension.
   */
  protected interface Vector<T> {
    /**
     * @return The value of the {@link Vector}.
     */
    T value();

    void reset();
  }
}

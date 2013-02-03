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

/**
 * <p>
 * {@link Metric} is the fundamental model for a type of telemetry.
 * </p>
 * 
 * <p>
 * Metrics employ label-orientation, meaning that a given name has multiple
 * dimensions that are defined by exported labels.
 * </p>
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public interface Metric {
  /**
   * Clear all mutations made in this Metric, resetting it back to its pristine
   * state.
   */
  void ResetAll();
}

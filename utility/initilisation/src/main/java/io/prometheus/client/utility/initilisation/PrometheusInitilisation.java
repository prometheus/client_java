/**
 * Copyright 2014 Prometheus Team Licensed under the Apache License, Version 2.0
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

package io.prometheus.client.utility.initilisation;

import io.prometheus.client.utility.standardexports.StandardExports;
import io.prometheus.client.utility.hotspot.Hotspot;
import io.prometheus.client.Prometheus;

public class PrometheusInitilisation {
  private static StandardExports standardExports;
  private static Hotspot hotspot; 
  /**
   * Initialize Prometheus client, and export default metrics.
   *
   * It is recommended that it is invoked early in the cycle of the main
   * class' main block. This calls {@link io.prometheus.client.Prometheus.defaultInitialize},
   * which is required for metrics to be exposed.
   */
  public static void initialize() {
    Prometheus.defaultInitialize();
    if (standardExports == null) {
        standardExports = new StandardExports();
        Prometheus.defaultAddPreexpositionHook(standardExports);
        hotspot = new Hotspot();
        Prometheus.defaultAddPreexpositionHook(hotspot);
    }
  }
}

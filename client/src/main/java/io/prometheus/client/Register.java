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

package io.prometheus.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Provide a quick way for registering {@link Metric} with the system.
 * </p>
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {
  /**
   * <p>
   * The {@link Metric} name.
   * </p>
   */
  public String name();

  /**
   * <p>
   * The human-readable documentation string for the {@link Metric}.
   * </p>
   */
  public String docstring();

  /**
   * <p>
   * Base labels to be associated with the {@link Metric}.
   * </p>
   * <p>
   * They must be done in the following manner due to the limitation of Java
   * Annotations:
   * <ul>
   * <li>An even number of elements,</li>
   * <li>{@code k1, v1, k2, v2}}</li>
   * </ul>
   * </p>
   */
  public String[] baseLabels() default {};
}

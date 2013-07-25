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
 * Register {@link Metric} and their derivatives with the {@link Prometheus}
 * registrar across all runtime libraries and their dependencies.
 * </p>
 *
 * <p>
 * The purpose of this <em>runtime</em> annotation is to provide a common
 * fragment to search for across an entire server's classpath for metrics to
 * export. Due to nuances in the underlying Java Virtual Machine
 * implementations, not every class referenced in an application's transitive
 * closure will be loaded.
 * <em>This can prevent telemetry from being found and registered!</em> Each
 * {@link Metric} decorated with {@link Metric} will be found and registered for
 * exposition across all libraries your server depends on.
 * </p>
 *
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {
}

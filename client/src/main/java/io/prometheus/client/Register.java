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
 * Register {@link io.prometheus.client.metrics.Metric} and their derivatives with the
 * {@link Prometheus} registrar across all runtime libraries and their dependencies and the
 * <em>class path</em>.
 * </p>
 *
 * <p>
 * The purpose of this <em>runtime</em> annotation is to provide a common fragment to search for
 * across an entire server's classpath for metrics to export.  Due to nuances in the underlying
 * Java Virtual Machine implementations and behaviors around both class loading and initialization
 * (i.e., behaviors outside of Prometheus' and your direct control), not every class referenced in
 * an application's transitive closure and its classpath will be loaded and initialized, unless it
 * is referenced by a dependent type, which is itself referenced by a root type in the
 * application.</p>
 *
 * <p><strong>Not using this <em>optional</em> annotation may prevent expected telemetry from being
 * found and registered</strong>!  This is to say, metric consumers may not find the metrics they
 * want without it.  Each {@link io.prometheus.client.metrics.Metric} decorated with
 * {@link Register} will be found and registered for exposition across all libraries your server
 * depends on.  <strong>This registration property can be beneficial for authors of shared
 * infrastructure libraries that are used by multiple teams</strong>!
 * </p>
 *
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {
}

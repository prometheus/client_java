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

package io.prometheus.client.examples.jvmstat;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;


/**
 * <p>Provide an example of using the jvmstat support classes.</p>
 *
 * <p>
 * This example builds strongly on the one found in the <em>guice</em> workflow in that it adds
 * an extra {@link Prometheus.ExpositionHook} through the use of a {@link
 * com.google.inject.multibindings.Multibinder} found in {@link
 * io.prometheus.client.examples.jvmstat.Module}.
 * </p>
 *
 * @see Module
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@Singleton
public class Main {
  public static void main(final String[] arguments) {
    final Injector injector = Guice.createInjector(
        new io.prometheus.client.examples.guice.Module(), new Module());
    final io.prometheus.client.examples.guice.Main main = injector.getInstance(
        io.prometheus.client.examples.guice.Main.class);

    main.run();
  }
}


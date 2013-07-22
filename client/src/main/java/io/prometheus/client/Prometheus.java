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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.Instant;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import io.prometheus.client.metrics.*;
import io.prometheus.client.utility.Clock;
import io.prometheus.client.utility.SystemClock;

/**
 * <p>
 * {@link Prometheus} manages the registration and exposition of {@link io.prometheus.client.metrics.Metric} instances.
 * </p>
 *
 * <p>
 * You can apply the patterns from examples in the following classes' Javadocs:
 * <ul>
 *   <li>
 *       {@link Counter}
 *   </li>
 *   <li>
 *       {@link Gauge}
 *   </li>
 *   <li>
 *       {@link Summary}
 *   </li>
 * </ul>
 * </p>
 *
 * <em>Important:</em> To initialize the whole stack, call
 * {@link Prometheus#defaultInitialize()} <em>once</em> somewhere in your main function.
 *
 * @see io.prometheus.client.metrics.Metric
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Prometheus {
  private static final Logger log = LoggerFactory.getLogger(Prometheus.class);

  private static final Gson serializer = new GsonBuilder()
      .registerTypeAdapter(AtomicDouble.class, new AtomicDoubleSerializer())
      .registerTypeAdapter(Counter.class, new Counter.Serializer())
      .registerTypeAdapter(Gauge.class, new Gauge.Serializer())
      .registerTypeAdapter(Summary.class, new Summary.Serializer()).create();

  private static final Prometheus defaultPrometheus = new Prometheus();

  private final ConcurrentHashMap<Metric, Instant> metrics =
      new ConcurrentHashMap<Metric, Instant>();
  private final Clock clock = new SystemClock();

  private void register(final Metric m) {
    if (metrics.putIfAbsent(m, clock.now()) != null) {
      log.warn(String.format("Metric %s is already registered!", m));
    }
  }

  private void dumpProto(final OutputStream o) throws IOException {
    final Instant start = clock.now();
    final Counter.Partial requests = Telemetry.telemetryRequests.newPartial();
    final Summary.Partial latencies = Telemetry.telemetryGenerationLatencies.newPartial();
    try {
      for (final Metric m : metrics.keySet()) {
        m.dump().writeDelimitedTo(o);
      }
      requests.withDimension("result", "success");
      latencies.withDimension("result", "success");
    } catch (final IOException e) {
      requests.withDimension("result", "failure");
      latencies.withDimension("result", "failure");
      throw e;
    } catch (final RuntimeException e) {
      requests.withDimension("result", "failure");
      latencies.withDimension("result", "failure");
      throw e;
    } finally {
      final double duration = clock.now().getMillis() - start.getMillis();

      requests.apply().increment();
      latencies.apply().observe(duration);
    }
  }

  private void dumpJson(final Writer writer) throws IOException {
    final Instant start = clock.now();
    final Counter.Partial requests = Telemetry.telemetryRequests.newPartial();
    final Summary.Partial latencies = Telemetry.telemetryGenerationLatencies.newPartial();
    try {
      final JsonArray array = new JsonArray();
      for (final Metric m : metrics.keySet()) {
        array.add(serializer.toJsonTree(m));
      }
      writer.write(array.toString());
      requests.withDimension("result", "success");
      latencies.withDimension("result", "success");
    } catch (final IOException e) {
      requests.withDimension("result", "failure");
      latencies.withDimension("result", "failure");
      throw e;
    } catch (final RuntimeException e) {
      requests.withDimension("result", "failure");
      latencies.withDimension("result", "failure");
      throw e;
    } finally {
      final double duration = clock.now().getMillis() - start.getMillis();

      requests.apply().increment();
      latencies.apply().observe(duration);
    }
  }

  private static void defaultRegister(final Metric m) {
    defaultPrometheus.register(m);
  }

  /**
   * <p>
   * Dump all metrics registered via {@link Register} to the provided
   * {@link Writer} in JSON.
   * </p>
   */
  public static void defaultDumpJson(final Writer writer) throws IOException {
    defaultPrometheus.dumpJson(writer);
  }

  /**
   * <p>
   * Dump all metrics registered via {@link Register} to the provided
   * {@link OutputStream} in varint-encoded record-length delimited Protocol
   * Buffer messages of {@link io.prometheus.client.Metrics.MetricFamily}.
   * </p>
   */
  public static void defaultDumpProto(final OutputStream o) throws IOException {
    defaultPrometheus.dumpProto(o);
  }

  private Collection<Field> collectAnnotatedFields() {
    final Reflections reflections =
        new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
            .setScanners(new FieldAnnotationsScanner()));

    return reflections.getFieldsAnnotatedWith(Register.class);
  }

  private void initialize() {
    final Instant start = clock.now();
    final Gauge.Partial duration = Telemetry.telemetryInitializationTime.newPartial();

    try {
      final Collection<Field> fields = collectAnnotatedFields();
      for (final Field field : fields) {
        final boolean wasAccessible = field.isAccessible();
        final String candidateName = field.getDeclaringClass().getCanonicalName();
        try {
          // Explicitly load the class to invoke any static blocks and
          // initializers.
          final Class<?> klass = Class.forName(candidateName);
          if (klass == null) {
            continue;
          }

          final Register annotation = field.getAnnotation(Register.class);
          if (annotation == null) {
            continue;
          }

          if (!wasAccessible) {
            field.setAccessible(true);
          }

          final Metric metric = (Metric) field.get(klass);

          register(metric);
        } catch (final ClassNotFoundException e) {
          System.err.printf("Could not find %s\n", candidateName);
        } catch (final IllegalAccessException e) {
          System.err.printf("Not allowed to access %s\n", field);
        } finally {
          if (!wasAccessible) {
            field.setAccessible(false);
          }
        }
      }
      duration.withDimension("result", "success");
    } catch (final RuntimeException e) {
      duration.withDimension("result", "failure");
      throw e;
    } finally {
      final float elapsed = (clock.now().getMillis() - start.getMillis());
      duration.apply().set(elapsed);
    }
  }

  /**
   * <p>
   * Register all {@link Metric} instances and their derivatives according to
   * the classpath findability discussion in {@link Register}.
   * </p>
   * 
   * <p>
   * Important Usage Notes:
   * 
   * <ul>
   * <li>Calling this is a <em>prerequisite</em> for successful Prometheus
   * usage, meaning if it is never called, no metrics will be exposed.</li>
   * <li>It is recommended that it is invoked early in the cycle of the main
   * class' main block.</li>
   * <li>While idempotent, it should be called only once.</li>
   * </ul>
   * </p>
   * 
   */
  public static void defaultInitialize() {
    defaultPrometheus.initialize();
  }
}

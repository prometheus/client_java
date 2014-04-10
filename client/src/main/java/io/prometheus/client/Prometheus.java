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

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Gauge;
import io.prometheus.client.metrics.Metric;
import io.prometheus.client.metrics.Summary;
import io.prometheus.client.utility.Clock;
import io.prometheus.client.utility.SystemClock;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * {@link Prometheus} manages the registration and exposition of
 * {@link io.prometheus.client.metrics.Metric} instances.
 * </p>
 *
 * <p>
 * You can apply the patterns from examples in the following classes' Javadocs:
 * <ul>
 * <li>
 * {@link Counter}</li>
 * <li>
 * {@link Gauge}</li>
 * <li>
 * {@link Summary}</li>
 * </ul>
 * </p>
 *
 * <em>Important:</em> To initialize the whole stack, call
 * {@link Prometheus#defaultInitialize()} <em>once</em> somewhere in your main
 * function.
 *
 * @see io.prometheus.client.metrics.Metric
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@ThreadSafe
public class Prometheus {
  private static final Logger log = Logger.getLogger(Prometheus.class.getName());

  private static final Gson serializer = new GsonBuilder()
      .registerTypeAdapter(AtomicDouble.class, new AtomicDoubleSerializer())
      .registerTypeAdapter(Counter.class, new Counter.Serializer())
      .registerTypeAdapter(Gauge.class, new Gauge.Serializer())
      .registerTypeAdapter(Summary.class, new Summary.Serializer()).create();

  private static final Prometheus defaultPrometheus = new Prometheus();

  private final Clock clock = new SystemClock();
  private final ConcurrentHashMap<Metric, Metric> metrics = new ConcurrentHashMap<Metric, Metric>();
  private final ConcurrentHashMap<ExpositionHook, Object> preexpositionHooks =
      new ConcurrentHashMap();

  private void register(final Metric m) {
    final Metric existing = metrics.putIfAbsent(m, m);

    if (existing == null) {
      log.log(Level.FINE, String.format("Registered %s", m));
    } else {
      if (existing != m) {
        log.log(Level.WARNING, String.format(
            "Cannot register %s, because %s is registered in its place.", m, existing));
      }
    }
  }

  private void dumpProto(final OutputStream o) throws IOException {
    final long start = clock.nowMs();

    runPreexpositionHooks();

    final Counter.Partial requests = Telemetry.telemetryRequests.newPartial();
    final Summary.Partial latencies = Telemetry.telemetryGenerationLatencies.newPartial();
    try {
      for (final Metric m : metrics.keySet()) {
        m.dump().writeDelimitedTo(o);
      }
      requests.labelPair("result", "success");
      latencies.labelPair("result", "success");
    } catch (final IOException e) {
      requests.labelPair("result", "failure");
      latencies.labelPair("result", "failure");
      throw e;
    } catch (final RuntimeException e) {
      requests.labelPair("result", "failure");
      latencies.labelPair("result", "failure");
      throw e;
    } finally {
      final double duration = clock.nowMs() - start;

      requests.apply().increment();
      latencies.apply().observe(duration);
    }
  }

  @Deprecated
  private void dumpJson(final Writer writer) throws IOException {
    final long start = clock.nowMs();

    runPreexpositionHooks();

    final Counter.Partial requests = Telemetry.telemetryRequests.newPartial();
    final Summary.Partial latencies = Telemetry.telemetryGenerationLatencies.newPartial();
    try {
      final JsonArray array = new JsonArray();
      for (final Metric m : metrics.keySet()) {
        array.add(serializer.toJsonTree(m));
      }
      writer.write(array.toString());
      requests.labelPair("result", "success");
      latencies.labelPair("result", "success");
    } catch (final IOException e) {
      requests.labelPair("result", "failure");
      latencies.labelPair("result", "failure");
      throw e;
    } catch (final RuntimeException e) {
      requests.labelPair("result", "failure");
      latencies.labelPair("result", "failure");
      throw e;
    } finally {
      final double duration = clock.nowMs() - start;

      requests.apply().increment();
      latencies.apply().observe(duration);
    }
  }

  /**
   * <p>
   * Register a {@link Metric} with Prometheus
   * </p>
   */
  public static void defaultRegister(final Metric m) {
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
    final long start = clock.nowMs();
    final Gauge.Partial duration = Telemetry.initializeTime.newPartial();

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
      duration.labelPair("result", "success");
    } catch (final RuntimeException e) {
      duration.labelPair("result", "failure");
      throw e;
    } finally {
      final float elapsed = clock.nowMs() - start;
      duration.apply().set(elapsed);
    }
  }

  private void addPreexpositionHook(final ExpositionHook h) {
    preexpositionHooks.putIfAbsent(h, this);
  }

  private void runPreexpositionHooks() {
    for (final ExpositionHook hook : preexpositionHooks.keySet()) {
      hook.run();
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

  public static void defaultAddPreexpositionHook(final ExpositionHook h) {
    defaultPrometheus.addPreexpositionHook(h);
  }

  /**
   * <p>
   * A management hook to be run prior to each metric exposition request.
   * </p>
   */
  public static interface ExpositionHook extends Runnable {
  }
}

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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;
import io.prometheus.client.metrics.*;
import io.prometheus.client.utility.Clock;
import io.prometheus.client.utility.SystemClock;
import io.prometheus.client.utility.labels.Reserved;
import org.joda.time.Instant;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * {@link Registry} is a repository for various {@link Metric} registrations and
 * is responsible for centralized exposition of server state.
 * </p>
 * 
 * <p>
 * Users are advised to stick with a pattern similar to this:
 * </p>
 * 
 * <pre>
 * {@code
 * package example;
 * 
 * import io.prometheus.client.Registry;
 * import io.prometheus.client.metrics.Counter;
 * 
 * public class CashRegister {
 *   @Register(name = "cash_register_operations_total", docstring = "The total sum of cash register operations " +
 *       "partitioned by operation type.", baseLabels = {"storeType", "bakery"})
 *   private static final Counter operations = new Counter();
 * 
 *   private static final OPERATION_KEY = "operation";
 *   private static final DIVISION = "division";
 *   private static final RESULT_KEY = "result";
 *   private static final SUCCESS = "success";
 *   private static final FAILURE = "failure";
 * 
 *   public float divide(dividend float, divisor float) {
 *     bool erroneous = false;
 * 
 *     try {
 *       return dividend / divisor;
 *     } catch (ArithmeticException e) {
 *       erroneous = true;
 *     } finally {
 *       tallyOperation(erroneous, DIVISION);
 *     }
 *   }
 * 
 *   private static tallyOperation(final bool erroneous, final String operation) {
 *     final String result erroneous ? FAILURE_KEY : SUCCESS_KEY;
 * 
 *     operations.Increment(ImmutableMap.of(OPERATION_KEY, operation, RESULT_KEY, result));
 *   }
 * }
 * }
 * </pre>
 * 
 * … and somewhere along the line, the user will need to invoke
 * {@link #defaultInitialize()} to initialize the whole stack.
 * 
 * @see Metric
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Registry {
  /**
   * <p>
   * Empty labels are provided as a singleton helper for folks to lazy to write
   * no labels.
   * </p>
   */
  private static final Map<String, String> EMPTY_LABELS = ImmutableMap.<String, String> of();
  /**
   * <p>
   * A Java properties key that if set to {@code TRUE} will cause the telemetry
   * system to abort of invalid input has been fed into it.
   * </p>
   */
  private static final String PROMETHEUS_CLIENT_ABORTONMISUSE = "prometheus.client.abortonmisuse";
  private static final String TRUE = "TRUE";

  private static final String SERIALIZE_BASE_LABELS = "baseLabels";
  private static final String SERIALIZE_DOCSTRING = "docstring";
  private static final String SERIALIZE_METRIC = "metric";

  private static final Logger log = LoggerFactory.getLogger(Registry.class);

  /**
   * <p>
   * Users of Prometheus client will register metrics via the {@link Register}
   * annotation or
   * {@link #defaultRegister(String, String, java.util.Map, io.prometheus.client.metrics.Metric)}
   * mechanism.
   * </p>
   */
  private static final Registry defaultRegistry = new Registry();


  private final Clock clock = new SystemClock();
  private final Map<Map<String, String>, Vector> baseLabelsToMetrics = new MapMaker().makeMap();
  private final Gson serializer;

  @VisibleForTesting
  Registry() {
    // XXX: Evaluate a way to support fluent serializer support.
    serializer =
        new GsonBuilder().registerTypeAdapter(Vector.class, new EntrySerializer())
            .registerTypeAdapter(Gauge.class, new Gauge.Serializer())
            .registerTypeAdapter(Counter.class, new Counter.Serializer())
            .registerTypeAdapter(AtomicDouble.class, new AtomicDoubleSerializer())
            .registerTypeAdapter(Histogram.class, new Histogram.Serializer()).create();
  }

  /**
   * <p>
   * Register a {@link Metric} with this {@link Registry}.
   * </p>
   * 
   * <p>
   * Whether or not a registration occurs depends on the following:
   * </p>
   * 
   * <ul>
   * <li>The metric's name is not empty.</li>
   * <li>The metric's name and label set have not already been registered.</li>
   * <li>The docstring is not empty.</li>
   * </ul>
   * 
   * <p>
   * Current limitations include the following:
   * </p>
   * <ul>
   * <li>Ensuring that double-registration has not occurred.</li>
   * </ul>
   * 
   * @param name The candidate metric's name.
   * @param docstring The docstring for the metric to help someone unfamiliar
   *        with it understand and develop an overview thereof within a few
   *        seconds of reading it.
   * @param baseLabels Verbatim labels that are embedded into the registered
   *        metric and displayed on exposition.
   * @param metric The metric to be registered.
   * @throws IllegalArgumentException,IllegalStateException if
   *         {@link #PROMETHEUS_CLIENT_ABORTONMISUSE} has been set to
   *         {@code TRUE} and invalid registrations have been provided.
   */
  public void register(final String name, final String docstring,
      final Map<String, String> baseLabels, final Metric metric) {
    // XXX: Add expensive cross-validation of double-name registration,
    // defaulting to off.
    try {
      validateArguments(name, docstring, baseLabels);

      final ImmutableMap.Builder labelSetBuilder = new ImmutableMap.Builder();
      if (baseLabels != null) {
        labelSetBuilder.putAll(baseLabels);
      }
      labelSetBuilder.put(Reserved.NAME.label(), name);

      final Map<String, String> labelSet = labelSetBuilder.build();

      validateLabelSet(labelSet);

      baseLabelsToMetrics.put(labelSet, new Vector(labelSet, docstring, metric));
    } catch (final IllegalArgumentException e) {
      final String candidateValue = System.getProperty(PROMETHEUS_CLIENT_ABORTONMISUSE);
      log.error("Could not register {}...", name);
      if (!TRUE.equalsIgnoreCase(candidateValue)) {
        throw e;
      }
    } catch (final IllegalStateException e) {
      log.error("Could not register {}...", name);
      final String candidateValue = System.getProperty(PROMETHEUS_CLIENT_ABORTONMISUSE);
      if (!TRUE.equalsIgnoreCase(candidateValue)) {
        throw e;
      }
    }
  }

  private void validateArguments(final String name, final String docstring,
      final Map<String, String> baseLabels) throws IllegalArgumentException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "empty or null name is not allowed"
        + ".");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(docstring), "empty or null docstring is "
        + "not allowed.");
    if (baseLabels != null) {
      Preconditions.checkArgument(!baseLabels.containsKey(Reserved.NAME.label()),
          "base labels may not contain " + "reserved 'name' label.");
    }
  }

  private void validateLabelSet(final Map<String, String> labelSet) throws IllegalArgumentException {
    Preconditions.checkState(!baseLabelsToMetrics.containsKey(labelSet),
        String.format("label set %s is already registered.", labelSet));
  }

  /**
   * <p>
   * An {@link io.prometheus.client.Registry.Vector} is the internal
   * representation of a {@link Metric} and its associated metadata.
   * </p>
   */
  private static final class Vector {
    private final Map<String, String> baseLabels;
    private final String docstring;
    private final Metric metric;

    private Vector(final Map<String, String> baseLabels, final String docstring, final Metric metric) {
      this.baseLabels = baseLabels;
      this.docstring = docstring;
      this.metric = metric;
    }
  }

  /**
   * <p>
   * {@link EntrySerializer} provides the correct JSON modeling of the
   * registry's entries.
   * </p>
   */
  private final class EntrySerializer implements JsonSerializer<Vector> {
    @Override
    public JsonElement serialize(final Vector src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();

      container.add(SERIALIZE_BASE_LABELS, context.serialize(src.baseLabels));
      container.addProperty(SERIALIZE_DOCSTRING, src.docstring);
      container.add(SERIALIZE_METRIC, context.serialize(src.metric));

      return container;
    }
  }

  /**
   * <p>
   * Dump this {@link Registry} to a consumer.
   * </p>
   * 
   * @param writer The destination for the JSON representation.
   * @throws IOException If an dumping anomaly occurs.
   */
  public void dumpToWriter(final Writer writer) throws IOException {
    final Instant start = clock.now();

    Telemetry.updateStandard();

    boolean erroneous = true;
    try {
      final JsonArray array = new JsonArray();
      for (final Vector entry : baseLabelsToMetrics.values()) {
        array.add(serializer.toJsonTree(entry));
      }
      writer.write(array.toString());
      erroneous = false;
    } finally {
      final float duration = clock.now().getMillis() - start.getMillis();
      if (erroneous) {
        Telemetry.telemetryRequests.increment(ImmutableMap.of("result", "failure"));
        Telemetry.telemetryGenerationLatencies.add(ImmutableMap.of("result", "failure"), duration);
      } else {
        Telemetry.telemetryRequests.increment(ImmutableMap.of("result", "success"));
        Telemetry.telemetryGenerationLatencies.add(ImmutableMap.of("result", "success"), duration);
      }
    }
  }

  /**
   * {@inheritDoc #register}
   */
  public static void defaultRegister(final String name, final String docstring,
      final Map<String, String> baseLabels, final Metric metric) {
    defaultRegistry.register(name, docstring, baseLabels, metric);
  }

  /**
   * {@inheritDoc #dumpToWriter}
   */
  public static void defaultDumpToWriter(final Writer writer) throws IOException {
    defaultRegistry.dumpToWriter(writer);
  }

  private Collection<Field> collectAnnotatedFields() {
    final Reflections reflections =
        new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
            .setScanners(new FieldAnnotationsScanner()));

    return reflections.getFieldsAnnotatedWith(Register.class);
  }

  /**
   * <p>
   * Perform all of the requested registrations.
   * </p>
   */
  public void initialize() {
    boolean erroneous = true;
    final Instant start = clock.now();

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

          if (annotation.baseLabels().length % 2 != 0) {
            throw new IllegalArgumentException(String.format(
                "baseLabels %s for %s are not zippable.", annotation.baseLabels(),
                annotation.name()));
          }

          final ImmutableMap.Builder<String, String> baseLabels = ImmutableMap.builder();
          for (int i = 0; i < annotation.baseLabels().length / 2; i++) {
            baseLabels.put(annotation.baseLabels()[i], annotation.baseLabels()[i + 1]);
          }

          register(annotation.name(), annotation.docstring(), baseLabels.build(), metric);

          erroneous = false;
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
    } finally {
      final float duration = (clock.now().getMillis() - start.getMillis());
      if (erroneous) {
        Telemetry.telemetryInitializationTime.incrementBy(ImmutableMap.of("result", "failure"),
            duration);
      } else {
        Telemetry.telemetryInitializationTime.incrementBy(ImmutableMap.of("result", "success"),
            duration);
      }
    }
  }

  /**
   * {@inheritDoc #initialize}
   */
  public static void defaultInitialize() {
    defaultRegistry.initialize();
  }

  /**
   * <p>
   * A convenience helper for empty labels.
   * </p>
   */
  public static Map<String, String> emptyLabels() {
    return EMPTY_LABELS;
  }
}

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

import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;


/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Counter extends StatelessGenerator<Counter.Vector> {
  public void set(final Map<String, String> labels, final double value) {
    final Vector vector = forLabels(labels);
    vector.value().getAndSet(value);
  }

  public void increment(final Map<String, String> labels) {
    final Vector vector = forLabels(labels);
    vector.value().addAndGet(1);
  }

  public void decrement(final Map<String, String> labels) {
    final Vector vector = forLabels(labels);
    vector.value().addAndGet(-1);
  }

  public void incrementBy(final Map<String, String> labels, final double delta) {
    final Vector vector = forLabels(labels);
    vector.value().addAndGet(delta);
  }

  public void decrementBy(final Map<String, String> labels, final double delta) {
    final Vector vector = forLabels(labels);
    vector.value().addAndGet(-1 * delta);
  }

  @Override
  protected Vector newVector() {
    return new Vector();
  }

  protected class Vector implements StatelessGenerator.Vector<AtomicDouble> {
    private final AtomicDouble value = new AtomicDouble();

    @Override
    public AtomicDouble value() {
      return value;
    }

    @Override
    public void reset() {}
  }

  /**
   * <p>
   * A standard {@link JsonSerializer} for {@link Counter}.
   * </p>
   */
  public static class Serializer implements JsonSerializer<Counter> {
    @Override
    public JsonElement serialize(final Counter src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();
      container.addProperty("type", "counter");
      final JsonArray values = new JsonArray();
      for (final Map<String, String> labelSet : src.vectors.keySet()) {
        final JsonObject element = new JsonObject();
        element.add("labels", context.serialize(labelSet));
        final Vector vector = src.vectors.get(labelSet);
        element.add("value", context.serialize(vector.value()));
        values.add(element);
      }
      container.add("value", values);
      return container;
    }
  }
}

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

package io.prometheus.client.utility;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * <p>
 * {@link AtomicFloat} provides a atomic interface around {@link Float} types,
 * achieved by proxying the Float bits in {@link AtomicInteger}.
 * </p>
 * 
 * @see AtomicInteger
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class AtomicFloat {
  private final AtomicInteger base = new AtomicInteger(Float.floatToIntBits(0f));

  public AtomicFloat() {}

  public AtomicFloat(final float value) {
    base.set(Float.floatToIntBits(value));
  }

  public float get() {
    return Float.intBitsToFloat(base.get());
  }

  public void set(final float value) {
    base.set(Float.floatToIntBits(value));
  }

  public float floatValue() {
    return Float.intBitsToFloat(base.get());
  }

  public float getAndSet(final float value) {
    return Float.intBitsToFloat(base.getAndSet(Float.floatToIntBits(value)));
  }

  public boolean weakCompareAndSet(final float expect, final float update) {
    return base.weakCompareAndSet(Float.floatToIntBits(expect), Float.floatToIntBits(update));
  }

  public double doubleValue() {
    return (double) floatValue();
  }

  public int intValue() {
    return (int) floatValue();
  }

  public long longValue() {
    return (long) floatValue();
  }

  public float incrementAndGet() {
    while (true) {
      final float current = get();
      final float next = current + 1;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return next;
      }
    }
  }

  public float getAndIncrement() {
    while (true) {
      final float current = get();
      final float next = current + 1;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return current;
      }
    }
  }

  public float decrementAndGet() {
    while (true) {
      final float current = get();
      final float next = current - 1;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return next;
      }
    }
  }

  public float getAndDecrement() {
    while (true) {
      final float current = get();
      final float next = current - 1;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return current;
      }
    }
  }

  public float getAndAdd(final float delta) {
    while (true) {
      final float current = get();
      final float next = current + delta;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return current;
      }
    }
  }

  public float addAndGet(final float delta) {
    while (true) {
      final float current = get();
      final float next = current + delta;
      if (base.compareAndSet(Float.floatToIntBits(current), Float.floatToIntBits(next))) {
        return next;
      }
    }
  }

  public static class Serializer implements JsonSerializer<AtomicFloat> {
    @Override
    public JsonElement serialize(final AtomicFloat src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      return new JsonPrimitive(Float.valueOf(src.floatValue()).toString());
    }
  }
}

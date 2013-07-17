package io.prometheus.client.metrics;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * <p>
 * A {@link JsonSerializer} for converting {@link AtomicDouble} into an
 * acceptable value for {@link com.google.gson.Gson}.
 * </p>
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class AtomicDoubleSerializer implements JsonSerializer<AtomicDouble> {
  @Override
  public JsonElement serialize(final AtomicDouble src, final Type typeOfSrc,
      final JsonSerializationContext context) {
    return new JsonPrimitive(src.doubleValue());
  }
}

package io.prometheus.client.exporter.common.formatter;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;

class DoubleUtil {
  private static final Unsafe UNSAFE;
  private static final long COUNT_OFFSET;
  private static final long VALUE_OFFSET;
  private static final ThreadLocal<StringBuilder> STRING_BUILDER_CACHE =
      new ThreadLocal<StringBuilder>();

  static {
    Class<Unsafe> clazz = Unsafe.class;
    try {
      Field field = clazz.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe) field.get(null);

      Class<?> superClass = StringBuilder.class.getSuperclass();
      COUNT_OFFSET = UNSAFE.objectFieldOffset(superClass.getDeclaredField("count"));
      VALUE_OFFSET = UNSAFE.objectFieldOffset(superClass.getDeclaredField("value"));
    } catch (NoSuchFieldException e) {
      throw new Error();
    } catch (IllegalAccessException ex) {
      throw new Error();
    }
  }

  /**
   * To prevent generate string objects.
   *
   * @param writer
   * @param v
   * @throws IOException
   */
  static void append(Writer writer, double v) throws IOException {
    StringBuilder builder = STRING_BUILDER_CACHE.get();
    if (builder == null) {
      builder = new StringBuilder();
      STRING_BUILDER_CACHE.set(builder);
    }

    builder.append(v);

    byte[] value = (byte[]) UNSAFE.getObject(builder, VALUE_OFFSET);
    int count = UNSAFE.getInt(builder, COUNT_OFFSET);

    for (int a = 0; a < count; a++) {
      writer.write(value[a]);
    }

    UNSAFE.getAndSetInt(builder, COUNT_OFFSET, 0);
  }
}

package io.prometheus.client.exporter.common.formatter;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;

class DoubleUtil {
  private static Unsafe unsafe;
  private static long countOffset;
  private static long valueOffset;
  private static ThreadLocal<StringBuilder> cache;
  private static boolean initialized = true;

  static {
    try {
      Class<Unsafe> unsafeKlass = Unsafe.class;
      Field field = unsafeKlass.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (Unsafe) field.get(null);

      Class<?> superKlass = StringBuilder.class.getSuperclass();
      countOffset = unsafe.objectFieldOffset(superKlass.getDeclaredField("count"));
      valueOffset = unsafe.objectFieldOffset(superKlass.getDeclaredField("value"));
      cache = new ThreadLocal<StringBuilder>();
    } catch (Exception e) {
      initialized = false;
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
    if (v == Double.POSITIVE_INFINITY) {
      writer.write("+Inf");
      return;
    } else if (v == Double.NEGATIVE_INFINITY) {
      writer.write("-Inf");
      return;
    }

    if (initialized) {
      StringBuilder builder = cache.get();
      try {
        if (builder == null) {
          builder = new StringBuilder(32);
          cache.set(builder);
        }

        builder.append(v);

        int count = unsafe.getInt(builder, countOffset);
        byte[] value = (byte[]) unsafe.getObject(builder, valueOffset);

        for (int idx = 0; idx < count; idx++) {
          writer.write(value[idx]);
        }

      } finally {
        unsafe.getAndSetInt(builder, countOffset, 0);
      }
    } else {
      writer.write(Double.toString(v));
    }
  }
}

package io.prometheus.client.exporter.common.formatter;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;

class DoubleUtil {
  private static Unsafe unsafe;
  private static long countOffset;
  private static long valueOffset;
  private static ThreadLocal<StringBuilder> stringBuilderCache;
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
      stringBuilderCache = new ThreadLocal<StringBuilder>();
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
    if (initialized) {
      StringBuilder builder = stringBuilderCache.get();
      if (builder == null) {
        builder = new StringBuilder();
        stringBuilderCache.set(builder);
      }

      builder.append(v);

      byte[] value = (byte[]) unsafe.getObject(builder, valueOffset);
      int count = unsafe.getInt(builder, countOffset);

      for (int a = 0; a < count; a++) {
        writer.write(value[a]);
      }

      unsafe.getAndSetInt(builder, countOffset, 0);
    } else {
      writer.write(Double.toString(v));
    }
  }
}

package io.prometheus.metrics.core.metrics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

final class ClassicOnlyAccumulatorStressSupport {
  private static final Field CELLS = field(ClassicOnlyAccumulator.class, "cells");
  private static final Field EPOCH = field(ClassicOnlyAccumulator.class, "epoch");
  private static final Field THREAD_CELL = field(ClassicOnlyAccumulator.class, "threadCell");
  private static final Method SET_ADD = method(Set.class, "add", Object.class);

  private ClassicOnlyAccumulatorStressSupport() {}

  static Object createStalledCell(ClassicOnlyAccumulator accumulator) {
    try {
      Object cell = ((ThreadLocal<?>) THREAD_CELL.get(accumulator)).get();
      SET_ADD.invoke(CELLS.get(accumulator), cell);
      Field registered = field(cell.getClass(), "registered");
      ((AtomicBoolean) registered.get(cell)).set(true);
      long epoch = ((AtomicLong) EPOCH.get(accumulator)).get();
      field(cell.getClass(), "writingEpoch").setLong(cell, epoch);
      Object buffer = ((Object[]) field(cell.getClass(), "buffers").get(cell))[(int) (epoch & 1)];
      ((long[]) field(buffer.getClass(), "buckets").get(buffer))[0] = 1;
      field(buffer.getClass(), "count").setLong(buffer, 1);
      field(buffer.getClass(), "sum").setDouble(buffer, 1.0);
      return cell;
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Unable to model a paused writer", e);
    }
  }

  static void releaseStalledCell(Object cell) {
    try {
      field(cell.getClass(), "writingEpoch").setLong(cell, -1);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Unable to resume a paused writer", e);
    }
  }

  private static Field field(Class<?> type, String name) {
    try {
      Field field = type.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
    try {
      return type.getMethod(name, parameterTypes);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
}

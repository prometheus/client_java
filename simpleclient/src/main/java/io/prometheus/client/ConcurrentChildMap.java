package io.prometheus.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstraction for the internal labels-to-child concurrent map.
 * <p>
 * The default implementation is based on a {@link ConcurrentHashMap} but users who are
 * interested in performance can provide an optimized implementation (which could be
 * garbage-free for example).
 * <p>
 * Implementations must have a no-arg or default constructor.
 */
public interface ConcurrentChildMap<Child> extends ConcurrentMap<List<String>, Child> {

  interface ChildFactory<Child> {
    Child newChild(String[] labels);
  }

  Child labels(ChildFactory<Child> childFactory, String... labelValues);

  Child labels(ChildFactory<Child> childFactory, String v1);

  Child labels(ChildFactory<Child> childFactory, String v1, String v2);

  Child labels(ChildFactory<Child> childFactory, String v1, String v2, String v3);

  Child labels(ChildFactory<Child> childFactory, String v1, String v2, String v3, String v4);

  void setChild(Child child, String... labelValues);

  void remove(String... labelValues);

  /**
   * The default {@link ConcurrentHashMap}-based implementation.
   */
  static class ConcurrentChildHashMap<Child> extends ConcurrentHashMap<List<String>, Child>
    implements ConcurrentChildMap<Child> {

    @Override
    public Child labels(ChildFactory<Child> childFactory, String... labelValues) {
      List<String> key = Arrays.asList(labelValues);
      Child c = get(key);
      if (c != null) {
        return c;
      }
      for (String label: labelValues) {
        if (label == null) {
          throw new IllegalArgumentException("Label cannot be null.");
        }
      }
      Child c2 = childFactory.newChild(labelValues);
      Child tmp = putIfAbsent(key, c2);
      return tmp == null ? c2 : tmp;
    }

    @Override
    public Child labels(ChildFactory<Child> childFactory, String v1) {
      return labels(childFactory, arr(v1));
    }

    @Override
    public Child labels(ChildFactory<Child> childFactory, String v1, String v2) {
      return labels(childFactory, arr(v1, v2));
    }

    @Override
    public Child labels(ChildFactory<Child> childFactory, String v1, String v2, String v3) {
      return labels(childFactory, arr(v1, v2, v3));
    }

    @Override
    public Child labels(ChildFactory<Child> childFactory, String v1, String v2, String v3, String v4) {
      return labels(childFactory, arr(v1, v2, v3, v4));
    }

    @Override
    public void setChild(Child child, String... labelValues) {
      put(Arrays.asList(labelValues), child);
    }

    @Override
    public void remove(String... labelValues) {
      remove(Arrays.asList(labelValues));
    }

    private static String[] arr(String... arr) {
      return arr;
    }
  }
}

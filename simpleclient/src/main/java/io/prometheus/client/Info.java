package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Info metric, key-value pairs.
 *
 * Examples of Info include, build information, version information, and potential target metadata,
 * The first provided state will be the default.
 *
 * <p>
 * Example enumeration:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Info buildInfo = Info.build()
 *         .name("your_build_info").help("Build information.")
 *         .register();
 *
 *     void func() {
 *          // Your code here.
 *         buildInfo.info("branch", "HEAD", "version", "1.2.3", "revision", "e0704b");
 *     }
 *   }
 * }
 * </pre>
 */
public class Info extends SimpleCollector<Info.Child> implements Counter.Describable {
  
  Info(Builder b) {
    super(b);
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Info> {
    @Override
    public Info create() {
      if (!unit.isEmpty()) {
        throw new IllegalStateException("Info metrics cannot have a unit.");
      }
      return new Info(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Info. Ensures required fields are provided.
   *
   *  @param name The name of the metric
   *  @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   *  Return a Builder to allow configuration of a new Info.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(labelNames);
  }


  /**
   * The value of a single Info.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {

    private Map<String, String> value = Collections.emptyMap();
    private List<String> labelNames;

    private Child(List<String> labelNames) {
      this.labelNames = labelNames;
    }

    /**
     * Set the info.
     */
    public void info(Map<String, String> v) {
      for (String key : v.keySet()) {
        checkMetricLabelName(key);
      }
      for (String label : labelNames) {
        if (v.containsKey(label)) {
          throw new IllegalArgumentException("Info and its value cannot have the same label name.");
        }
      }
      this.value = v;
    }
    /**
     * Set the info.
     *
     * @param v labels as pairs of key values
     */
    public void info(String... v) {
      if (v.length % 2 != 0) {
        throw new IllegalArgumentException("An even number of arguments must be passed");
      }
      Map<String, String> m = new TreeMap<String, String>();
      for (int i = 0; i < v.length; i+=2) {
        m.put(v[i], v[i+1]);
      }
      info(m);
    }

    /**
     * Get the info.
     */
    public Map<String, String> get() {
      return value;
    }
  }

  // Convenience methods.
  /**
   * Set the info on the info with no labels.
   */
  public void info(String... v) {
    noLabelsChild.info(v);
  }

  /**
   * Set the info on the info with no labels.
   *
   * @param v labels as pairs of key values
   */
  public void info(Map<String, String> v) {
    noLabelsChild.info(v);
  }

  /**
   * Get the the info.
   */
  public Map<String, String> get() {
    return noLabelsChild.get();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      Map<String, String> v = c.getValue().get();
      List<String> names = new ArrayList<String>(labelNames);
      List<String> values = new ArrayList<String>(c.getKey());
      for(Map.Entry<String, String> l: v.entrySet()) {
        names.add(l.getKey());
        values.add(l.getValue());
      }
      samples.add(new MetricFamilySamples.Sample(fullname + "_info", names, values, 1.0));
    }

    return familySamplesList(Type.INFO, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.singletonList(
            new MetricFamilySamples(fullname, Type.INFO, help, Collections.<MetricFamilySamples.Sample>emptyList()));
  }

}

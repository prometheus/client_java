package io.prometheus.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Enumeration metric, to track which of a set of states something is in.
 *
 * The first provided state will be the default.
 *
 * <p>
 * Example Enumeration:
 * <pre>
 * {@code
 *   class YourClass {
 *     static final Enumeration taskState = Enumeration.build()
 *         .name("task_state").help("State of the task.")
 *         .states("stopped", "starting", "running")
 *         .register();
 *
 *     void stop() {
 *          // Your code here.
 *          taskState.state("stopped")
 *     }
 *   }
 * }
 * </pre>
 *
 * You can also use a Java Enum:
 * <pre>
 *   class YourClass {
 *     public enum yourEnum {
 *       STOPPED,
 *       STARTING,
 *       RUNNING,
 *     }
 *     static final Enumeration taskState = Enumeration.build()
 *         .name("task_state").help("State of the task.")
 *         .states(yourEnum.class)
 *         .register();
 *
 *     void stop() {
 *          // Your code here.
 *          taskState.state(yourEnum.STOPPED)
 *     }
 *   }
 * }
 * </pre>
 *
 * @since 0.10.0
 */
public class Enumeration extends SimpleCollector<Enumeration.Child> implements Counter.Describable {

  private final Set<String> states;

  Enumeration(Builder b) {
    super(b);
    for (String label : labelNames) {
      if (label.equals(fullname)) {
        throw new IllegalStateException("Enumeration cannot have a label named the same as its metric name.");
      }
    }
    states = b.states;
    initializeNoLabelsChild();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, Enumeration> {

    private Set<String> states;

    public Builder states(String... s) {
      if (s.length == 0) {
        throw new IllegalArgumentException("There must be at least one state");
      }
      // LinkedHashSet so we can know which was the first state.
      states = new LinkedHashSet();
      states.addAll(Arrays.asList(s));
      return this;
    }

    /**
     * Take states from the names of the values in an Enum class.
     */
    public Builder states(Class e) {
      Object[] vals = e.getEnumConstants();
      String[] s = new String[vals.length];
      for(int i = 0; i < vals.length; i++) {
        s[i] = ((Enum)vals[i]).name();
      }
      return states(s);
    }

    @Override
    public Enumeration create() {
      if (states == null) {
        throw new IllegalStateException("Enumeration states must be specified.");
      }
      if (!unit.isEmpty()) {
        throw new IllegalStateException("Enumeration metrics cannot have a unit.");
      }
      dontInitializeNoLabelsChild = true;
      return new Enumeration(this);
    }
  }

  /**
   *  Return a Builder to allow configuration of a new Enumeration. Ensures required fields are provided.
   *
   *  @param name The name of the metric
   *  @param help The help string of the metric
   */
  public static Builder build(String name, String help) {
    return new Builder().name(name).help(help);
  }

  /**
   *  Return a Builder to allow configuration of a new Enumeration.
   */
  public static Builder build() {
    return new Builder();
  }

  @Override
  protected Child newChild() {
    return new Child(states);
  }


  /**
   * The value of a single Enumeration.
   * <p>
   * <em>Warning:</em> References to a Child become invalid after using
   * {@link SimpleCollector#remove} or {@link SimpleCollector#clear}.
   */
  public static class Child {

    private String value;
    private final Set<String> states;

    private Child(Set<String> states) {
      this.states = states;
      value = states.iterator().next(); // Initialize with the first state.
    }

    /**
     * Set the state.
     */
    public void state(String s) {
      if (!states.contains(s)) {
        throw new IllegalArgumentException("Unknown state " + s);
      }
      value = s;
    }

    /**
     * Set the state.
     */
    public void state(Enum e) {
      state(e.name());
    }

    /**
     * Get the state.
     */
    public String get() {
      return value;
    }
  }

  // Convenience methods.
  /**
   * Set the state on the enum with no labels.
   */
  public void state(String s) {
    noLabelsChild.state(s);
  }

  /**
   * Set the state on the enum with no labels.
   */
  public void state(Enum e) {
    noLabelsChild.state(e);
  }

  /**
   * Get the value of the Enumeration.
   */
  public String get() {
    return noLabelsChild.get();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for(Map.Entry<List<String>, Child> c: children.entrySet()) {
      String v = c.getValue().get();
      List<String> labelNamesWithState = new ArrayList<String>(labelNames);
      labelNamesWithState.add(fullname);
      for(String s : states) {
        List<String> labelValuesWithState = new ArrayList<String>(c.getKey());
        labelValuesWithState.add(s);
        samples.add(new MetricFamilySamples.Sample(fullname, labelNamesWithState, labelValuesWithState, s.equals(v) ? 1.0 : 0.0));
      }
    }

    return familySamplesList(Type.STATE_SET, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.singletonList(
            new MetricFamilySamples(fullname, Type.STATE_SET, help, Collections.<MetricFamilySamples.Sample>emptyList()));
  }

}

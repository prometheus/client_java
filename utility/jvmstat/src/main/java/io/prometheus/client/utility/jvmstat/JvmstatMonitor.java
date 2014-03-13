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

package io.prometheus.client.utility.jvmstat;

import io.prometheus.client.Prometheus;
import io.prometheus.client.metrics.Gauge;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * {@link JvmstatMonitor} provides HotSpot-specific JVM metrics through the {@link
 * sun.jvmstat.monitor.MonitoredVm} facilities.
 * </p>
 * <p/>
 * <p>
 * These <em>low-level</em> metrics are defined in the C++ bowels of the HotSpot VM through
 * internal measurement types.  The VM exposes these through the <em>HSPerfData</em> interface
 * for customers.  Typically ones accesses this data via a MMAPed direct buffer—namely Java native
 * I/O (nio).
 * </p>
 * <p/>
 * <p>
 * <h1>Important Notes</h1>
 * Due to inconsistencies of virtual machine packages and vendoring, the following remarks should
 * be carefully observed:
 * <ul>
 * <li>
 * Users may need to explicitly add {@code ${JAVA_HOME}/lib/tools.jar} to the <em>CLASSPATH</em>
 * when using this helper.  This is because the {@code sun.jvmstat.monitor} package hierarchy
 * is not a part of the standard Java library.  These packages are, however, typically included
 * in Sun-, Oracle-, and OpenJDK-provided virtual machines, which is to say they are ubiquitous
 * in most deployment environments.
 * </li>
 * <li>
 * Users may need to explicitly set {@code -XX:+UsePerfData} in the VM's flags to enable
 * low-level telemetric export.
 * </li>
 * </ul>
 * </p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 * @see Prometheus#addPreexpositionHook(io.prometheus.client.Prometheus.ExpositionHook)
 * @see sun.jvmstat.monitor.MonitoredVm
 * @see {@code perfdata/resources/aliasmap}
 */
public class JvmstatMonitor implements Prometheus.ExpositionHook {
  private static final Gauge.Builder gaugePrototype = Gauge.newBuilder()
      .namespace("jvmstat");
  private static final Logger log = Logger.getLogger(JvmstatMonitor.class.getName());

  private final MonitoredVm bridge;

  private final ClassLoaderInstrumentation clsInstrumentation =
      new ClassLoaderInstrumentation();
  private final NativeCodeCompilerInstrumentation nccInstrumentation =
      new NativeCodeCompilerInstrumentation();
  private final GarbageCollectionInstrumentation gcInstrumentation =
      new GarbageCollectionInstrumentation();
  private final ManagedMemoryInstrumentation mmInstrumentation =
      new ManagedMemoryInstrumentation();

  /**
   * <p>Create a {@link JvmstatMonitor} for the local virtual machine associated with this
   * specific Java server.</p>
   *
   * @throws {@link AttachmentError} if the VM cannot be attached to.
   */
  public JvmstatMonitor() throws AttachmentError {
    this(getLocalVM());
  }

  /**
   * <p>Create a {@link JvmstatMonitor} monitoring bridge with a supplied {@link
   * sun.jvmstat.monitor.MonitoredVm}.</p>
   * <p/>
   * <p><strong>This is only useful for reusing an existing {@link sun.jvmstat.monitor.MonitoredVm}
   * or for testing purposes.</strong>  Users are encouraged to use the {@link #JvmstatMonitor()}
   * default constructor.</p>
   *
   * @param b
   */
  public JvmstatMonitor(final MonitoredVm b) {
    bridge = b;
  }

  @Override
  public void run() {
    /*
     * N.B.: There are a few ways we could make this cleaner, through the current
     * implementation is just fine.  Here are a few candidates:
      *
      * - Direct buffer access of HSPerfData MMAP.
      * -- Caveat: We would need to implement our own HSPerfData processor, which becomes a huge
      *            point of fragility with even minor HotSpot releases.
      * - Extending the Metric and its children's mutation model to use a callback when a value
      *   is requested.
      * -- Caveat: Unimplemented, but would need to be really careful with the API's design.
      * - Persistent VmListener registered with HotSpot.
      * -- Caveat: HotSpot just uses polls as well in the underlying implementation for event
      *            dispatching.
     */
    final List<Monitor> monitors;
    try {
      monitors = bridge.findByPattern(".*");
      if (monitors == null) {
        return;
      }
    } catch (final MonitorException ex) {
      log.warning(String.format("could not extract telemetry: %s", ex));
      return;
    } catch (final PatternSyntaxException ex) {
      log.warning(String.format("could not extract telemetry: %s", ex));
      return;
    }

    for (final Monitor monitor : monitors) {
      /*
       * Dynamically visit any metrics if they are found.  Unfound metrics are never
       * registered and exported, because we do not want to clutter the namespace and
       * confuse users.
       *
       * The VM offers no contract about metric order, so it seems imprudent to accumulate
       * state and infer the existence of extra metrics solely by the presence of an earlier one.
       */

      if (clsInstrumentation.visit(monitor)) {
        continue;
      }
      if (nccInstrumentation.visit(monitor)) {
        continue;
      }
      if (gcInstrumentation.visit(monitor)) {
        continue;
      }
      if (mmInstrumentation.visit(monitor)) {
        continue;
      }
    }
  }

  private static MonitoredVm getLocalVM() throws AttachmentError {
    final String name = ManagementFactory.getRuntimeMXBean().getName();
    final int pidOffset = name.indexOf('@');
    final int pid;
    try {
      pid = Integer.valueOf(name.substring(0, pidOffset));
    } catch (final IndexOutOfBoundsException ex) {
      throw new AttachmentError("illegal instance name", ex);
    } catch (final NumberFormatException ex) {
      throw new AttachmentError("illegal instance name format", ex);
    }

    try {
      final HostIdentifier hostId = new HostIdentifier((String) null);
      final MonitoredHost host = MonitoredHost.getMonitoredHost(hostId);
      final String localAddr = String.format("//%d?mode=r", pid);
      final VmIdentifier id = new VmIdentifier(localAddr);

      return host.getMonitoredVm(id);
    } catch (final URISyntaxException ex) {
      throw new AttachmentError("invalid target URI", ex);
    } catch (final MonitorException ex) {
      throw new AttachmentError("could not resolve host", ex);
    }
  }

  /**
   * <p>A {@link AttachmentError} indicates that it was impossible to attach to the requested
   * virtual machine.</p>
   */
  public static class AttachmentError extends Exception {
    AttachmentError(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * <p>Provide visibility about the internals of the JVM's class loader.</p>
   * <p/>
   * <h1>Generated Metrics</h1>
   * <h2>{@code jvmstat_classloader_operations_total}</h2>
   * This metric tracks the number of classloader operations by event type.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>{@code event}: The type of event.
   * <ul>
   * <li>{@code loaded}: The virtual machine loaded a class.  This occurs when the virtual
   * machine starts and builds its initial class dependency graph.  Alternatively it may occur at
   * runtime when using the reflection facilities or programmatic class loader: {@code
   * Class.forName("java.lang.String")}.  As noted in {@code unloaded}, this can also
   * occur if a unneeded class is unloaded due to memory pressure in the <em>permanent generation
   * </em> and later needs to be reloaded.  The virtual machine's default behavior is to try to
   * keep all classes statically loaded for the lifetime of the process.  See See {@code
   * ClassLoadingService::notify_class_loaded}</li>
   * <li>{@code unloaded}: The virtual machine unloaded a class.  This event is likely to be rare
   * and will likely occur when either <em>permanent generation</em> memory space is too small for
   * all of the needed classes, or memory pressure occurs from excessive class loading churn.
   * Examples for this latter case of churn and memory pressure may be from using code generation
   * facilities from mock or dependency injection frameworks, though this case is most likely to
   * occur only in test suite runs.  Most likely, memory pressure is a result from using a
   * poorly-designed dynamic language on the virtual machine that unintelligently uses code
   * generation for dynamic types, thereby blasting the <em>permanent generation</em> with
   * leaky class metadata descriptors.  See @{code
   * ClassLoadingService::notify_class_unloaded}.</li>
   * <li>{@code initialized}: The virtual machine initializes a class.  The process of initializing
   * a class is different from loading it, in that initialization takes the process of loading one
   * important step further: <em>it initializes all static fields and instantiates all objects
   * required for the fulfillment of that</em>, including performing further class loading and
   * initialization of dependent classes.  The initialization process includes running a class'
   * static initializers: {@code static {}} blocks.  See VMS 2.16.4 and 2.16.5 and {@code
   * InstanceClass::initialize} for a further discussion.</li>
   * </ul>
   * </li>
   * </ul>
   */
  public static class ClassLoaderInstrumentation implements MonitorVisitor {
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("classloader");

    private final AtomicBoolean statesOnce = new AtomicBoolean(false);
    private final AtomicBoolean sizesOnce = new AtomicBoolean(false);
    private final AtomicBoolean durationsOnce = new AtomicBoolean(false);

    private Gauge states;
    private Gauge sizes;
    private Gauge durations;

    public boolean visit(final Monitor metric) {
      switch (metric.getName()) {
        case "java.cls.loadedClasses":
        case "java.cls.unloadedClasses":
        case "sun.cls.initializedClasses":
          return visitClassEvents(metric);

        case "sun.cls.loadedBytes":
          return visitSizes(metric);

        case "sun.classloader.findClassTime":
        case "sun.cls.parseClassTime":
          return visitDurations(metric);
      }

      return false;
    }

    private boolean visitDurations(final Monitor metric) {
      if (durations == null && durationsOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("duration_ms")
            .documentation("The time it has taken the classloader to perform loadings partitioned by operation.")
            .labelNames("operation")
            .build();
      }
      final String name = metric.getName();
      final String base = name.substring(name.lastIndexOf(".") + 1);
      final String event = base.substring(0, base.length() - 9);
      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }
      durations.newPartial()
          .labelPair("event", event)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitSizes(final Monitor metric) {
      if (sizes == null && sizesOnce.getAndSet(true) == false) {
        sizes = gaugePrototype
            .name("loaded")
            .documentation("The number of bytes the classloader has loaded.")
            .build();
      }
      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }
      sizes.newPartial()
          .apply()
          .set(value);
      return true;
    }

    private boolean visitClassEvents(final Monitor metric) {
      if (states == null && statesOnce.getAndSet(true) == false) {
        states = gaugePrototype
            .name("operations_total")
            .documentation("The number of classes the loader has touched by disposition.")
            .labelNames("event")
            .build();
      }
      final String name = metric.getName();
      final String base = name.substring(name.lastIndexOf(".") + 1);
      final String event = base.substring(0, base.length() - 7);
      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }
      states.newPartial()
          .labelPair("event", event)
          .apply()
          .set(value);

      return true;
    }
  }

  public static class NativeCodeCompilerInstrumentation implements MonitorVisitor {
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("jit");

    private final AtomicBoolean compilationOnce = new AtomicBoolean(false);
    private final AtomicBoolean durationOnce = new AtomicBoolean(false);

    private Gauge compilations;
    private Gauge durations;

    public boolean visit(final Monitor metric) {
      switch (metric.getName()) {
            /*
             * Incorporate sun.ci.threads for accurate counting.  It is unlikely that there will
             * be more than two threads at any time.  If we see index "2", we will need to revisit
             * this exposition bridge.
             */
        case "sun.ci.compilerThread.0.compiles":
        case "sun.ci.compilerThread.1.compiles":
        case "sun.ci.compilerThread.2.compiles":
          return visitCompilations(metric);

        case "sun.ci.compilerThread.0.time":
        case "sun.ci.compilerThread.1.time":
        case "sun.ci.compilerThread.2.time":
          return visitDurations(metric);
      }

      return false;
    }

    private boolean visitDurations(final Monitor metric) {
      if (durations == null && durationOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("compilation_time_ms")
            .documentation("The count of JIT compilation events.")
            .labelNames("thread")
            .build();
      }
      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }
      durations.newPartial()
          // Extract the thread index from the name.
          .labelPair("thread", metric.getName().substring(22, 23))
          .apply()
          .set(value);
      return true;
    }

    private boolean visitCompilations(final Monitor metric) {
      if (compilations == null && compilationOnce.getAndSet(true) == false) {
        compilations = gaugePrototype
            .name("compilation_count")
            .documentation("The count of JIT compilation events.")
            .labelNames("thread")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }
      compilations.newPartial()
          // Extract the thread index from the name.
          .labelPair("thread", metric.getName().substring(22, 23))
          .apply()
          .set(value);
      return true;
    }
  }

  public static class GarbageCollectionInstrumentation implements MonitorVisitor {
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("garbage_collection");

    final AtomicBoolean invocationsOnce = new AtomicBoolean(false);
    final AtomicBoolean durationsOnce = new AtomicBoolean(false);

    Gauge invocations;
    Gauge durations;

    /*
     * TODO: Add garbage collection failure mode registration instrumentation after coming to
     *       consensus on internal naming inside of the Virtual Machine.
     *
     * TODO: Add metrics for "full GC" events from failure modes.
     */
    public boolean visit(final Monitor metric) {
      switch (metric.getName()) {
        case "sun.gc.collector.0.invocations":
        case "sun.gc.collector.1.invocations":
          return visitInvocations(metric);

        case "sun.gc.collector.0.time":
        case "sun.gc.collector.1.time":
          return visitDurations(metric);
      }

      return false;
    }

    private boolean visitInvocations(final Monitor metric) {
      if (invocations == null && invocationsOnce.getAndSet(true) == false) {
        invocations = gaugePrototype
            .name("invocations_total")
            .labelNames("generation")
            .documentation("The total number of times the garbage collector has been invoked.")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }

      switch (metric.getName()) {
        case "sun.gc.collector.0.invocations":
          calculateInvocations("new", value);
        case "sun.gc.collector.1.invocations":
          calculateInvocations("old", value);
          break;
      }

      return true;
    }

    private void calculateInvocations(final String generation, final double time) {
      invocations.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(time);
    }

    private boolean visitDurations(final Monitor metric) {
      if (durations == null && durationsOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("durations_ms_total")
            .documentation("The total time spent in garbage collection for a generation.")
            .build();
      }

      final Double valueMicros = decodeMetric(metric);
      if (valueMicros == null) {
        return false;
      }
      final Double valueMillis = valueMicros / 1000;
      switch (metric.getName()) {
        case "sun.gc.collector.0.time":
          calculateDurations("new", valueMillis);
        case "sun.gc.collector.1.time":
          calculateDurations("old", valueMillis);
          break;
      }

      return true;
    }

    private void calculateDurations(final String generation, final double time) {
      durations.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(time);
    }
  }

  public static class ManagedMemoryInstrumentation implements MonitorVisitor {
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("managed_memory");

    private final AtomicBoolean agetableCohortsOnce = new AtomicBoolean(false);
    private final AtomicBoolean agetableCountOnce = new AtomicBoolean(false);
    private final AtomicBoolean generationLimitOnce = new AtomicBoolean(false);
    private final AtomicBoolean generationUsageOnce = new AtomicBoolean(false);

    // TODO: Discuss markOop and oopDesc types.
    private Gauge agetableCohorts;
    private Gauge agetableCount;
    private Gauge generationLimit;
    private Gauge generationUsage;

    public boolean visit(final Monitor metric) {
      switch (metric.getName()) {
        case "sun.gc.generation.0.agetable.bytes.00":
        case "sun.gc.generation.0.agetable.bytes.01":
        case "sun.gc.generation.0.agetable.bytes.02":
        case "sun.gc.generation.0.agetable.bytes.03":
        case "sun.gc.generation.0.agetable.bytes.04":
        case "sun.gc.generation.0.agetable.bytes.05":
        case "sun.gc.generation.0.agetable.bytes.06":
        case "sun.gc.generation.0.agetable.bytes.07":
        case "sun.gc.generation.0.agetable.bytes.08":
        case "sun.gc.generation.0.agetable.bytes.09":
        case "sun.gc.generation.0.agetable.bytes.10":
        case "sun.gc.generation.0.agetable.bytes.11":
        case "sun.gc.generation.0.agetable.bytes.12":
        case "sun.gc.generation.0.agetable.bytes.13":
        case "sun.gc.generation.0.agetable.bytes.14":
        case "sun.gc.generation.0.agetable.bytes.15":
          return visitSurvivorSpaceAgetableCohorts(metric);

        case "sun.gc.generation.0.agetable.size":
          return visitSurvivorSpaceAgetableCount(metric);

        case "sun.gc.generation.0.space.0.capacity":
        case "sun.gc.generation.0.space.1.capacity":
        case "sun.gc.generation.0.space.2.capacity":
        case "sun.gc.generation.1.space.0.capacity":
        case "sun.gc.generation.2.space.0.capacity":
          return visitGenerationLimits(metric);

        case "sun.gc.generation.0.space.0.used":
        case "sun.gc.generation.0.space.1.used":
        case "sun.gc.generation.0.space.2.used":
        case "sun.gc.generation.1.space.0.used":
        case "sun.gc.generation.2.space.0.used":
          return visitGenerationUsage(metric);
      }

      return false;
    }

    private boolean visitGenerationLimits(final Monitor metric) {
      if (generationLimit == null && generationLimitOnce.getAndSet(true) == false) {
        generationLimit = gaugePrototype
            .name("generation_limit_bytes")
            .labelNames("generation")
            .documentation("The total allocation/reservation of each managed memory region or generation.")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }

      switch (metric.getName()) {
        case "sun.gc.generation.0.space.0.capacity":
          calculateEdenGenerationLimit(value);
          break;

        case "sun.gc.generation.0.space.1.capacity":
          calculateSurvivorSpaceLimit("survivor0", value);
          break;

        case "sun.gc.generation.0.space.2.capacity":
          calculateSurvivorSpaceLimit("survivor1", value);
          break;

        case "sun.gc.generation.1.space.0.capacity":
          calculateOldGenerationLimit(value);
          break;

        case "sun.gc.generation.2.space.0.capacity":
          calculatePermGenerationLimit(value);
          break;
      }

      return true;
    }

    private void calculateEdenGenerationLimit(final double value) {
      generationLimit.newPartial()
          .labelPair("generation", "new")
          .apply()
          .set(value);
    }

    private void calculateSurvivorSpaceLimit(final String space, final double value) {
      generationLimit.newPartial()
          .labelPair("generation", space)
          .apply()
          .set(value);
    }

    private void calculateOldGenerationLimit(final double value) {
      generationLimit.newPartial()
          .labelPair("generation", "old")
          .apply()
          .set(value);
    }

    private void calculatePermGenerationLimit(final double value) {
      generationLimit.newPartial()
          .labelPair("generation", "permgen")
          .apply()
          .set(value);
    }

    private boolean visitGenerationUsage(final Monitor metric) {
      if (generationUsage == null && generationUsageOnce.getAndSet(true) == false) {
        generationUsage = gaugePrototype
            .name("generation_usage_bytes")
            .labelNames("generation")
            .documentation("The size used of each managed memory region or generation.")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }

      switch (metric.getName()) {
        case "sun.gc.generation.0.space.0.used":
          calculateEdenGenerationUsage(value);
          break;

        case "sun.gc.generation.0.space.1.used":
          calculateSurvivorSpaceUsage("survivor0", value);
          break;

        case "sun.gc.generation.0.space.2.used":
          calculateSurvivorSpaceUsage("survivor1", value);
          break;

        case "sun.gc.generation.1.space.0.used":
          calculateOldGenerationUsage(value);
          break;

        case "sun.gc.generation.2.space.0.used":
          calculatePermGenerationUsage(value);
          break;
      }

      return true;
    }

    private void calculateEdenGenerationUsage(final double value) {
      generationUsage.newPartial()
          .labelPair("generation", "new")
          .apply()
          .set(value);
    }

    private void calculateSurvivorSpaceUsage(final String space, final double value) {
      generationUsage.newPartial()
          .labelPair("generation", space)
          .apply()
          .set(value);
    }

    private void calculateOldGenerationUsage(final double value) {
      generationUsage.newPartial()
          .labelPair("generation", "old")
          .apply()
          .set(value);
    }

    private void calculatePermGenerationUsage(final double value) {
      generationUsage.newPartial()
          .labelPair("generation", "permgen")
          .apply()
          .set(value);
    }

    private boolean visitSurvivorSpaceAgetableCohorts(final Monitor metric) {
      if (agetableCohorts == null && agetableCohortsOnce.getAndSet(true) == false) {
        agetableCohorts = gaugePrototype
            .name("survivor_space_agetable_size_bytes")
            .labelNames("cohort")
            .documentation("A measure of the size of each survivor space agetable cohort.")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }

      final String cohort = metric.getName().substring(35, 37);
      agetableCohorts.newPartial()
          .labelPair("cohort", cohort)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitSurvivorSpaceAgetableCount(final Monitor metric) {
      if (agetableCount == null && agetableCountOnce.getAndSet(true) == false) {
        agetableCount = gaugePrototype
            .name("survivor_space_agetable_count")
            .documentation("The number of survivor space agetable cohorts.")
            .build();
      }

      final Double value = decodeMetric(metric);
      if (value == null) {
        return false;
      }

      agetableCount.newPartial()
          .apply()
          .set(value);

      return true;
    }
  }

  static interface MonitorVisitor {
    boolean visit(final Monitor m);
  }

  private static Double decodeMetric(final Monitor m) {
    final Object value = m.getValue();

    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return Double.valueOf((Long) value);
    } else {
      try {
        return Double.valueOf(value.toString());
      } catch (final NumberFormatException unused) {
        return null;
      }
    }
  }
}

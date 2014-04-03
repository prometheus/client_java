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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * {@link JvmstatMonitor} provides HotSpot-specific JVM metrics through the {@link
 * sun.jvmstat.monitor.MonitoredVm} facilities.
 * </p>
 *
 * <p>
 * These <em>low-level</em> metrics are defined in the C++ bowels of the HotSpot VM through
 * internal measurement types.  The VM exposes these through the <em>HSPerfData</em> interface
 * for customers.  Typically one accesses this data via a MMAPed direct buffer—namely Java native
 * I/O (nio).
 * </p>
 *
 * <p>
 * <h1>Important Notes</h1>
 * Due to inconsistencies of virtual machine packages and vendoring, the following remarks should
 * be carefully observed:
 * <ul>
 * <li>
 *   Users may need to explicitly add {@code ${JAVA_HOME}/lib/tools.jar} to the <em>CLASSPATH</em>
 *   when using this helper.  This is because the {@code sun.jvmstat.monitor} package hierarchy
 *   is not a part of the standard Java library.  These packages are, however, typically included
 *   in Sun-, Oracle-, and OpenJDK-provided virtual machines, which is to say they are ubiquitous
 *   in most deployment environments.
 * </li>
 * <li>
 *   Users <em>may</em> need to explicitly set {@code -XX:+UsePerfData} in the VM's flags to enable
 *   low-level telemetric export.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * The metrics that this class exposes are dependent upon the release of HotSpot, including even
 * minor releases.  Depending on that, it may be possible to use adapt this utility to be more
 * flexible by incorporating support for common metric aliases, as defined in {@code
 * sun/jvmstat/perfdata/resources/aliasmap}.
 * </p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 * @see Prometheus#addPreexpositionHook(io.prometheus.client.Prometheus.ExpositionHook)
 * @see sun.jvmstat.monitor.MonitoredVm
 */
public class JvmstatMonitor implements Prometheus.ExpositionHook {
  private static final int REFRESH_INTERVAL = 5;
  private static final Gauge.Builder gaugePrototype = Gauge.newBuilder()
      .namespace("jvmstat");
  private static final Logger log = Logger.getLogger(JvmstatMonitor.class.getName());

  private final MonitoredVm bridge;

  private final MonitorVisitor clsInstrumentation = new ClassLoaderInstrumentation();
  private final MonitorVisitor nccInstrumentation = new NativeCodeCompilerInstrumentation();
  private final MonitorVisitor gcInstrumentation = new GarbageCollectionInstrumentation();
  private final MonitorVisitor mmInstrumentation =  new ManagedMemoryInstrumentation();

  private final AtomicInteger refreshes = new AtomicInteger(0);
  private final ConcurrentHashMap<String, Monitor> monitors = new ConcurrentHashMap<>(400);

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
   *
   * <p><strong>This is only useful for reusing an existing {@link sun.jvmstat.monitor.MonitoredVm}
   * or for testing purposes.</strong>  Users are encouraged to use the {@link #JvmstatMonitor()}
   * default constructor.</p>
   *
   * @param b
   */
  public JvmstatMonitor(final MonitoredVm b) {
    bridge = b;
  }

  private void refreshMetrics() {
    final List<Monitor> monitors;
    try {
      monitors = bridge.findByPattern(".*");
    } catch (final MonitorException ex) {
      log.warning(String.format("could not extract telemetry: %s", ex));
      return;
    } catch (final PatternSyntaxException ex) {
      log.warning(String.format("could not extract telemetry: %s", ex));
      return;
    }
    if (monitors == null) {
      return;
    }
    for (final Monitor monitor : monitors) {
      this.monitors.putIfAbsent(monitor.getName(), monitor);
    }
  }

  @Override
  public void run() {
    if (refreshes.getAndIncrement() % REFRESH_INTERVAL == 0) {
      /* This model presumes that metrics are only added throughout the course of runtime and never
       * removed.  I think that is a reasonable assumption for now.  Handling the removal case
       * would be easy.
       */
      refreshMetrics();
    }
    /*
     * There are a few ways we could make this cleaner, through the current implementation is just
     * fine.  Here are a few candidates:
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
    for (final String monitorName : monitors.keySet()) {
      final Monitor monitor = monitors.get(monitorName);

      /*
       * Dynamically visit any metrics if they are found.  Unfound metrics are never
       * registered and exported, because we do not want to clutter the namespace and
       * confuse users.
       *
       * The VM offers no contract about metric order, so it seems imprudent to accumulate
       * state and infer the existence of extra metrics solely by the presence of an earlier one.
       */
      if (clsInstrumentation.visit(monitorName, monitor)) {
        continue;
      }
      if (nccInstrumentation.visit(monitorName, monitor)) {
        continue;
      }
      if (gcInstrumentation.visit(monitorName, monitor)) {
        continue;
      }
      if (mmInstrumentation.visit(monitorName, monitor)) {
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
   * <p>A {@link UnknownMonitorError} indicates that we have an invalid entry in the processing
   * pipeline.</p>
   */
  static class UnknownMonitorError extends IllegalArgumentException {
    UnknownMonitorError(final Monitor m) {
      super(String.format("unhandled jvmstat monitor: %s", m.getName()));
    }
  }

  /**
   * <p>Provide visibility about the internals of the JVM's class loader.</p>
   *
   * <h1>Generated Metrics</h1>
   * <h2>{@code jvmstat_classloader_operations_total}</h2>
   * This metric tracks the number of classloader operations by event type.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code event}: The type of event.
   * <ul>
   * <li>
   *   {@code loaded}: The virtual machine loaded a class.  This occurs when the virtual
   *   machine starts and builds its initial class dependency graph.  Alternatively it may occur at
   *   runtime when using the reflection facilities or programmatic class loader: {@code
   *   Class.forName("java.lang.String")}.  As noted in {@code unloaded}, this can also
   *   occur if an unneeded class is unloaded due to memory pressure in the <em>permanent
   *   generation</em> and later needs to be reloaded.  The virtual machine's default behavior is
   *   to try to keep all classes statically loaded for the lifetime of the process.  See {@code
   *   ClassLoadingService::notify_class_loaded} in the HotSpot source.
   * </li>
   * <li>
   *   {@code unloaded}: The virtual machine unloaded a class.  This event is likely to be rare
   *   and will likely occur when either <em>permanent generation</em> memory space is too small
   *   for all of the needed classes, or memory pressure occurs from excessive class loading churn.
   *   Examples for this latter case of churn and memory pressure may be from using code generation
   *   facilities from mock or dependency injection frameworks, though this case is most likely to
   *   occur only in test suite runs.  Most likely, memory pressure is a result from using a
   *   poorly-designed dynamic language on the virtual machine that unintelligently uses code
   *   generation for ad hoc type generation, thereby blasting the <em>permanent generation</em>
   *   with leaky class metadata descriptors.  See @{code
   *   ClassLoadingService::notify_class_unloaded} in the HotSpot source.
   * </li>
   * <li>
   *   {@code initialized}: The virtual machine initializes a class.  The process of initializing
   *   a class is different from loading it, in that initialization takes the process of loading
   *   one important step further: <em>it initializes all static fields and instantiates all
   *   objects required for the fulfillment of that class</em>, including performing further class
   *   loading and initialization of dependent classes.  The initialization process includes
   *   running a class' static initializers: {@code static {}} blocks.  Consult the <em>Virtual
   *   Machine Specification sections 2.16.4-5</em> and {@code InstanceClass::initialize} in the
   *   HotSpot source for a further discussion.
   * </li>
   * </ul>
   * </li>
   * </ul>
   *
   * <h2>{@code jvmstat_classloader_duration_ms}</h2>
   * This metric tracks the amount of time the class loader spends in various types of
   * operations.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code operation}: The operation type.
   * </li>
   * <li>
   *   {@code find}
   * </li>
   * <li>
   *   {@code parse}
   * </li>
   * </ul>
   *
   * <h2>{@code jvmstat_classloader_loaded_bytes}</h2>
   * This metric tracks the total number of bytes the classloader has loaded and processed.
   */
  public static class ClassLoaderInstrumentation implements MonitorVisitor {
    // This class has public visibility solely for Javadoc production.
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("classloader");

    private final AtomicBoolean statesOnce = new AtomicBoolean(false);
    private final AtomicBoolean sizesOnce = new AtomicBoolean(false);
    private final AtomicBoolean durationsOnce = new AtomicBoolean(false);

    private Gauge states;
    private Gauge sizes;
    private Gauge durations;

    public boolean visit(final String name, final Monitor monitor) {
      switch (name) {
        case "java.cls.loadedClasses":
        case "java.cls.unloadedClasses":
        case "sun.cls.initializedClasses":
          return visitClassEvents(name, monitor);

        case "sun.cls.loadedBytes":
          return visitSizes(monitor);

        case "sun.classloader.findClassTime":
        case "sun.cls.parseClassTime":
          return visitDurations(name, monitor);
      }

      return false;
    }

    private boolean remarkDuration(final String duration, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      durations.newPartial()
          .labelPair("operation", duration)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitDurations(final String name, final Monitor monitor) {
      if (durations == null && durationsOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("duration_ms")
            .documentation("The time it has taken the classloader to perform loadings partitioned by operation.")
            .labelNames("operation")
            .build();
      }

      switch (name) {
        case "sun.classloader.findClassTime":
          return remarkDuration("find", monitor);
        case "sun.cls.parseClassTime":
          return remarkDuration("parse", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean visitSizes(final Monitor monitor) {
      if (sizes == null && sizesOnce.getAndSet(true) == false) {
        sizes = gaugePrototype
            .name("loaded_bytes")
            .documentation("The number of bytes the classloader has loaded.")
            .build();
      }

      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      sizes.newPartial()
          .apply()
          .set(value);
      return true;
    }

    private boolean remarkClassEvent(final String event, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      states.newPartial()
          .labelPair("event", event)
          .apply()
          .set(value);
      return true;
    }

    private boolean visitClassEvents(final String name, final Monitor monitor) {
      if (states == null && statesOnce.getAndSet(true) == false) {
        states = gaugePrototype
            .name("operations_total")
            .documentation("The number of classes the loader has touched by disposition.")
            .labelNames("event")
            .build();
      }

      switch (name) {
        case "java.cls.loadedClasses":
          return remarkClassEvent("loaded", monitor);
        case "java.cls.unloadedClasses":
          return remarkClassEvent("unloaded", monitor);
        case "sun.cls.initializedClasses":
          return remarkClassEvent("initialized", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }
  }

  /**
   * <p>Provide visibility about the internals of the JVM's just-in-time (JIT) compiler.</p>
   *
   * <p>Even if you do not run your Java server with {@code -server} mode, these metrics will be
   * beneficial to understand how much is being optimized and whether a deeper analysis to find
   * classes to blacklist from JIT rewriting exist.</p>
   *
   * <h1>Generated Metrics</h1>
   * <h2>{@code jvmstat_jit_compilation_time_ms}</h2>
   * This metric tracks the amount of time the JIT spends compiling byte code into native code
   * partitioned by JIT thread.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code thread}: The thread ID associated with the work.
   * </li>
   * <ul>
   *
   * <h2>{@code jvmstat_jit_compilation_count}</h2>
   * This metric tracks the number of classes the JIT has compiled and potentially recompiled after
   * determining either the initial byte code definition or re-interpreted machine code
   * representation is suboptimal.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code thread}: The thread ID associated with the work.
   * </li>
   * <ul>
   */
  public static class NativeCodeCompilerInstrumentation implements MonitorVisitor {
    // This class has public visibility solely for Javadoc production.
    private static final Gauge.Builder gaugePrototype = JvmstatMonitor.gaugePrototype
        .subsystem("jit");

    private final AtomicBoolean compilationOnce = new AtomicBoolean(false);
    private final AtomicBoolean durationOnce = new AtomicBoolean(false);

    private Gauge compilations;
    private Gauge durations;

    public boolean visit(final String name, final Monitor monitor) {
      switch (name) {
        /*
         * Incorporate sun.ci.threads for accurate counting.  It is unlikely that there will
         * be more than two threads at any time.  If we see index "2", we will need to revisit
         * this exposition bridge.
         */
        case "sun.ci.compilerThread.0.compiles":
        case "sun.ci.compilerThread.1.compiles":
        case "sun.ci.compilerThread.2.compiles":
          return visitCompilations(name, monitor);

        case "sun.ci.compilerThread.0.time":
        case "sun.ci.compilerThread.1.time":
        case "sun.ci.compilerThread.2.time":
          return visitDurations(name, monitor);
      }

      return false;
    }

    private boolean remarkDuration(final String thread, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      durations.newPartial()
          .labelPair("thread", thread)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitDurations(final String name, final Monitor monitor) {
      if (durations == null && durationOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("compilation_time_ms")
            .documentation("The count of JIT compilation events.")
            .labelNames("thread")
            .build();
      }

      switch (name) {
        case "sun.ci.compilerThread.0.time":
          return remarkDuration("0", monitor);
        case "sun.ci.compilerThread.1.time":
          return remarkDuration("1", monitor);
        case "sun.ci.compilerThread.2.time":
          return remarkDuration("2", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean remarkCompilation(final String thread, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      compilations.newPartial()
          .labelPair("thread", thread)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitCompilations(final String name, final Monitor monitor) {
      if (compilations == null && compilationOnce.getAndSet(true) == false) {
        compilations = gaugePrototype
            .name("compilation_count")
            .documentation("The count of JIT compilation events.")
            .labelNames("thread")
            .build();
      }

      switch (name) {
        case "sun.ci.compilerThread.0.compiles":
          return remarkCompilation("0", monitor);
        case "sun.ci.compilerThread.1.compiles":
          return remarkCompilation("1", monitor);
        case "sun.ci.compilerThread.2.compiles":
          return remarkCompilation("2", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }
  }

  /**
   * <p>Provide visibility about the internals of the JVM's garbage collector.</p>
   *
   * <h1>Generated Metrics</h1>
   * <h2>{@code jvmstat_garbage_collection_invocations_total}</h2>
   * <p>Since JRE 1.1, the Java Virtual Machine has used a generational model for managing memory
   * due to object lifecycle in well-designed systems typically following an exponential
   * distribution whereby the majority of object born die young.  This means great efficiency gains
   * can be achieved when long-standing tenured objects are ignored (i.e., collected less
   * frequently) and the focus of collection becomes those newly-birthed objects</p>
   *
   * <pre>
   *  o  |
   *  b  | X
   *  j  | X
   *  e  | X  X
   *  c  | X  X
   *  t  | X  X  X
   *  s  | X  X  X
   *     | X  X  X  X  X  X
   *  #   —————————————————————
   *       0  1  2  3  4  5 … n
   *
   *      object cohort age
   * </pre>
   *
   * <p>This is to say, assuming a small amount of persistent in-memory state within a server, the
   * majority of objects are allocated during units of work (e.g., HTTP requests, RPCs, etc.) and
   * have no bearing or produce little by way of side effects that need to be retained later
   * in the life of the server.  <strong>This is the ideal case.</strong></p>
   *
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code generation}: The managed memory region name.
   * </li>
   * <ul>
   *
   * <h2>{@code jvmstat_garbage_collection_duration_ms_total}</h2>
   * This metric tracks the amount of time spent spent collecting the respective generation.
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code generation}: The managed memory region name.
   * </li>
   * <ul>
   */
  public static class GarbageCollectionInstrumentation implements MonitorVisitor {
    // This class has public visibility solely for Javadoc production.
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
    public boolean visit(final String name, final Monitor monitor) {
      switch (name) {
        case "sun.gc.collector.0.invocations":
        case "sun.gc.collector.1.invocations":
          return visitInvocations(name, monitor);

        case "sun.gc.collector.0.time":
        case "sun.gc.collector.1.time":
          return visitDurations(name, monitor);
      }

      return false;
    }

    private boolean remarkInvocation(final String generation, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      invocations.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitInvocations(final String name, final Monitor monitor) {
      if (invocations == null && invocationsOnce.getAndSet(true) == false) {
        invocations = gaugePrototype
            .name("invocations_total")
            .labelNames("generation")
            .documentation("The total number of times the garbage collector has been invoked.")
            .build();
      }

      switch (name) {
        case "sun.gc.collector.0.invocations":
          return remarkInvocation("new", monitor);
        case "sun.gc.collector.1.invocations":
          return remarkInvocation("old", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean remarkDuration(final String generation, final Monitor monitor) {
      final Double valueMicros = decodeMetric(monitor);
      if (valueMicros == null) {
        return false;
      }

      final Double valueMillis = valueMicros / 1000;

      durations.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(valueMillis);

      return true;
    }

    private boolean visitDurations(final String name, final Monitor monitor) {
      if (durations == null && durationsOnce.getAndSet(true) == false) {
        durations = gaugePrototype
            .name("durations_ms_total")
            .documentation("The total time spent in garbage collection for a generation.")
            .build();
      }

      switch (name) {
        case "sun.gc.collector.0.time":
          return remarkDuration("new", monitor);
        case "sun.gc.collector.1.time":
          return remarkDuration("old", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }
  }

  /**
   * <p>Provide visibility about the internals of the JVM memory regions.</p>
   *
   * <h1>Generated Metrics</h1>
   * <h2>{@code jvmstat_managed_memory_generation_limit_bytes}</h2>
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code generation}: The managed memory region name.
   * </li>
   * <ul>
   *
   * <h2>{@code jvmstat_managed_memory_generation_usage_bytes}</h2>
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code generation}: The managed memory region name.
   * </li>
   * <ul>
   *
   * <h2>{@code jvmstat_managed_memory_survivor_space_agetable_size_bytes}</h2>
   * <h3>Metric Dimensions</h3>
   * <ul>
   * <li>
   *   {@code cohort}: The agetable cohort.  (TODO: Discuss the agetable design.)
   * </li>
   * <ul>
   *
   * <h2>{@code jvmstat_managed_memory_survivor_space_agetable_count}</h2>
   *(TODO: Discuss the agetable design.)
   *
   * @see io.prometheus.client.utility.jvmstat.JvmstatMonitor.GarbageCollectionInstrumentation
   */
  public static class ManagedMemoryInstrumentation implements MonitorVisitor {
    // This class has public visibility solely for Javadoc production.
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

    public boolean visit(final String name, final Monitor monitor) {
      switch (name) {
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
          return visitSurvivorSpaceAgetableCohorts(name, monitor);

        case "sun.gc.generation.0.agetable.size":
          return visitSurvivorSpaceAgetableCount(monitor);

        case "sun.gc.generation.0.space.0.capacity":
        case "sun.gc.generation.0.space.1.capacity":
        case "sun.gc.generation.0.space.2.capacity":
        case "sun.gc.generation.1.space.0.capacity":
        case "sun.gc.generation.2.space.0.capacity":
          return visitGenerationLimits(name, monitor);

        case "sun.gc.generation.0.space.0.used":
        case "sun.gc.generation.0.space.1.used":
        case "sun.gc.generation.0.space.2.used":
        case "sun.gc.generation.1.space.0.used":
        case "sun.gc.generation.2.space.0.used":
          return visitGenerationUsage(name, monitor);
      }

      return false;
    }

    private boolean remarkGenerationLimit(final String generation, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      generationLimit.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(value);
      return true;
    }

    private boolean visitGenerationLimits(final String name, final Monitor monitor) {
      if (generationLimit == null && generationLimitOnce.getAndSet(true) == false) {
        generationLimit = gaugePrototype
            .name("generation_limit_bytes")
            .labelNames("generation")
            .documentation("The total allocation/reservation of each managed memory region or generation.")
            .build();
      }

      switch (name) {
        case "sun.gc.generation.0.space.0.capacity":
          return remarkGenerationLimit("eden", monitor);
        case "sun.gc.generation.0.space.1.capacity":
          return remarkGenerationLimit("survivor0", monitor);
        case "sun.gc.generation.0.space.2.capacity":
          return remarkGenerationLimit("survivor1", monitor);
        case "sun.gc.generation.1.space.0.capacity":
          return remarkGenerationLimit("old", monitor);
        case "sun.gc.generation.2.space.0.capacity":
          return remarkGenerationLimit("permgen", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean remarkGenerationUsage(final String generation, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      generationUsage.newPartial()
          .labelPair("generation", generation)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitGenerationUsage(final String name, final Monitor monitor) {
      if (generationUsage == null && generationUsageOnce.getAndSet(true) == false) {
        generationUsage = gaugePrototype
            .name("generation_usage_bytes")
            .labelNames("generation")
            .documentation("The size used of each managed memory region or generation.")
            .build();
      }

      switch (name) {
        case "sun.gc.generation.0.space.0.used":
          return remarkGenerationUsage("eden", monitor);
        case "sun.gc.generation.0.space.1.used":
          return remarkGenerationUsage("survivor0", monitor);
        case "sun.gc.generation.0.space.2.used":
          return remarkGenerationUsage("survivor1", monitor);
        case "sun.gc.generation.1.space.0.used":
          return remarkGenerationUsage("old", monitor);
        case "sun.gc.generation.2.space.0.used":
          return remarkGenerationUsage("permgen", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean remarkAgetableCohortSize(final String cohort, final Monitor monitor) {
      final Double value = decodeMetric(monitor);
      if (value == null) {
        return false;
      }

      agetableCohorts.newPartial()
          .labelPair("cohort", cohort)
          .apply()
          .set(value);

      return true;
    }

    private boolean visitSurvivorSpaceAgetableCohorts(final String name, final Monitor monitor) {
      if (agetableCohorts == null && agetableCohortsOnce.getAndSet(true) == false) {
        agetableCohorts = gaugePrototype
            .name("survivor_space_agetable_size_bytes")
            .labelNames("cohort")
            .documentation("A measure of the size of each survivor space agetable cohort.")
            .build();
      }

      switch (name) {
        case "sun.gc.generation.0.agetable.bytes.00":
          return remarkAgetableCohortSize("00", monitor);
        case "sun.gc.generation.0.agetable.bytes.01":
          return remarkAgetableCohortSize("01", monitor);
        case "sun.gc.generation.0.agetable.bytes.02":
          return remarkAgetableCohortSize("02", monitor);
        case "sun.gc.generation.0.agetable.bytes.03":
          return remarkAgetableCohortSize("03", monitor);
        case "sun.gc.generation.0.agetable.bytes.04":
          return remarkAgetableCohortSize("04", monitor);
        case "sun.gc.generation.0.agetable.bytes.05":
          return remarkAgetableCohortSize("05", monitor);
        case "sun.gc.generation.0.agetable.bytes.06":
          return remarkAgetableCohortSize("06", monitor);
        case "sun.gc.generation.0.agetable.bytes.07":
          return remarkAgetableCohortSize("07", monitor);
        case "sun.gc.generation.0.agetable.bytes.08":
          return remarkAgetableCohortSize("08", monitor);
        case "sun.gc.generation.0.agetable.bytes.09":
          return remarkAgetableCohortSize("09", monitor);
        case "sun.gc.generation.0.agetable.bytes.10":
          return remarkAgetableCohortSize("10", monitor);
        case "sun.gc.generation.0.agetable.bytes.11":
          return remarkAgetableCohortSize("11", monitor);
        case "sun.gc.generation.0.agetable.bytes.12":
          return remarkAgetableCohortSize("12", monitor);
        case "sun.gc.generation.0.agetable.bytes.13":
          return remarkAgetableCohortSize("13", monitor);
        case "sun.gc.generation.0.agetable.bytes.14":
          return remarkAgetableCohortSize("14", monitor);
        case "sun.gc.generation.0.agetable.bytes.15":
          return remarkAgetableCohortSize("15", monitor);
        default:
          throw new UnknownMonitorError(monitor);
      }
    }

    private boolean visitSurvivorSpaceAgetableCount(final Monitor monitor) {
      if (agetableCount == null && agetableCountOnce.getAndSet(true) == false) {
        agetableCount = gaugePrototype
            .name("survivor_space_agetable_count")
            .documentation("The number of survivor space agetable cohorts.")
            .build();
      }

      final Double value = decodeMetric(monitor);
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
    boolean visit(final String name, final Monitor monitor);
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

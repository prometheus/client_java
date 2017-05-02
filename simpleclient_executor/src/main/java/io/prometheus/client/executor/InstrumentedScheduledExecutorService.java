package io.prometheus.client.executor;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentedScheduledExecutorService implements ScheduledExecutorService {

  private static final AtomicLong NAME_COUNTER = new AtomicLong();
  private static final Counter ST_COUNTER;
  private static final Counter CT_COUNTER;
  private static final Gauge R_GAUGE;
  private static final Histogram D_HISTOGRAM;
  private static final Counter S_ONCE_COUNTER;
  private static final Counter SR_COUNTER;
  private static final Counter S_OVERRUN_COUNTER;
  private static final Histogram POP_HISTOGRAM;

  private final ScheduledExecutorService delegate;
  private final Counter.Child submittedCounter;
  private final Counter.Child completedCounter;
  private final Gauge.Child runningGauge;
  private final Histogram.Child durationHistogram;
  private final Counter.Child scheduledOnceCounter;
  private final Counter.Child scheduledRepetitivelyCounter;
  private final Counter.Child scheduledOverrunCounter;
  private final Histogram.Child percentOfPeriodHistogram;

  static {
    ST_COUNTER = Counter.build()
        .name("instrumented_scheduled_executor_service_submitted_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service submitted total count")
        .register();
    CT_COUNTER = Counter.build()
        .name("instrumented_scheduled_executor_service_completed_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service completed total count")
        .register();

    R_GAUGE = Gauge.build()
        .name("instrumented_scheduled_executor_service_running_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service running total count")
        .register();

    D_HISTOGRAM = Histogram.build()
        .name("instrumented_scheduled_executor_service_duration_seconds")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service duration (s)")
        .register();

    S_ONCE_COUNTER = Counter.build()
        .name("instrumented_scheduled_executor_service_scheduled_once_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service scheduled once count")
        .register();

    SR_COUNTER = Counter.build()
        .name("instrumented_scheduled_executor_service_scheduled_repetitively_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service scheduled repetitively count")
        .register();

    S_OVERRUN_COUNTER = Counter.build()
        .name("instrumented_scheduled_executor_service_scheduled_overrun_count")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor service scheduled overrun total count")
        .register();

    POP_HISTOGRAM = Histogram.build()
        .name("instrumented_scheduled_executor_service_period_percentage")
        .labelNames("serviceName")
        .help("Instrumented scheduled executor period percentage ( elapsed / period time )")
        .register();
  }


  public InstrumentedScheduledExecutorService(ScheduledExecutorService delegate) {
    this(delegate, "instrumented_scheduled_delegate_" + NAME_COUNTER.incrementAndGet());
  }

  public InstrumentedScheduledExecutorService(ScheduledExecutorService delegate, String name) {
    this.delegate = delegate;
    this.submittedCounter = ST_COUNTER.labels(name);
    this.completedCounter = CT_COUNTER.labels(name);
    this.runningGauge = R_GAUGE.labels(name);
    this.durationHistogram = D_HISTOGRAM.labels(name);
    this.scheduledOnceCounter = S_ONCE_COUNTER.labels(name);
    this.scheduledRepetitivelyCounter = SR_COUNTER.labels(name);
    this.scheduledOverrunCounter = S_OVERRUN_COUNTER.labels(name);
    this.percentOfPeriodHistogram = POP_HISTOGRAM.labels(name);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    scheduledOnceCounter.inc();
    InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(command);
    return delegate.schedule(instrumentedRunnable, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    scheduledOnceCounter.inc();
    InstrumentedCallable<V> instrumentedCallable = new InstrumentedCallable<V>(callable);
    return delegate.schedule(instrumentedCallable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    scheduledRepetitivelyCounter.inc();
    InstrumentedPeriodicRunnable instrumentedPeriodicRunnable = new InstrumentedPeriodicRunnable(
        command, period, unit);
    return delegate.scheduleAtFixedRate(instrumentedPeriodicRunnable, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit) {
    scheduledRepetitivelyCounter.inc();
    InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(command);
    return delegate.scheduleAtFixedRate(instrumentedRunnable, initialDelay, delay, unit);
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    submittedCounter.inc();
    return delegate.submit(new InstrumentedCallable<T>(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    submittedCounter.inc();
    return delegate.submit(new InstrumentedRunnable(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    submittedCounter.inc();
    return delegate.submit(new InstrumentedRunnable(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    submittedCounter.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAll(instrumented);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
      TimeUnit unit)
      throws InterruptedException {
    submittedCounter.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAll(instrumented, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    submittedCounter.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAny(instrumented);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    submittedCounter.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAny(instrumented, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    submittedCounter.inc();
    delegate.execute(new InstrumentedRunnable(command));
  }

  private <T> Collection<? extends Callable<T>> instrument(
      Collection<? extends Callable<T>> tasks) {
    final List<InstrumentedCallable<T>> instrumented = new ArrayList<InstrumentedCallable<T>>(
        tasks.size());
    for (Callable<T> task : tasks) {
      instrumented.add(new InstrumentedCallable<T>(task));
    }
    return instrumented;
  }

  private class InstrumentedPeriodicRunnable implements Runnable {

    private final Runnable command;

    private final long periodInSecs;

    InstrumentedPeriodicRunnable(Runnable command, long period, TimeUnit unit) {
      this.command = command;
      this.periodInSecs = unit.toSeconds(period);
    }

    @Override
    public void run() {
      runningGauge.inc();
      Histogram.Timer timer = durationHistogram.startTimer();
      try {
        command.run();
      } finally {
        final double elapsed = timer.observeDuration();
        runningGauge.dec();
        completedCounter.inc();
        if (elapsed > periodInSecs) {
          scheduledOverrunCounter.inc();
        }
        percentOfPeriodHistogram.observe(elapsed / periodInSecs);
      }
    }
  }

  private class InstrumentedRunnable implements Runnable {

    private final Runnable task;

    InstrumentedRunnable(Runnable task) {
      this.task = task;
    }

    @Override
    public void run() {
      runningGauge.inc();
      Histogram.Timer timer = durationHistogram.startTimer();
      try {
        task.run();
      } finally {
        timer.observeDuration();
        runningGauge.dec();
        completedCounter.inc();
      }
    }
  }

  private class InstrumentedCallable<T> implements Callable<T> {

    private final Callable<T> callable;

    InstrumentedCallable(Callable<T> callable) {
      this.callable = callable;
    }

    @Override
    public T call() throws Exception {
      runningGauge.inc();
      Histogram.Timer timer = durationHistogram.startTimer();
      try {
        return callable.call();
      } finally {
        timer.observeDuration();
        runningGauge.dec();
        completedCounter.inc();
      }
    }
  }
}

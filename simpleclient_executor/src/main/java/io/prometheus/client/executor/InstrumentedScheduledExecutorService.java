package io.prometheus.client.executor;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
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
  private static final Counter SUBMITTED_COUNTER;
  private static final Counter COMPLETED_COUNTER;
  private static final Gauge RUNNING_GAUGE;
  private static final Histogram DURATION_HISTOGRAM;
  private static final Counter SCHEDULED_ONCE_COUNTER;
  private static final Counter SCHEDULED_REPETITIVELY_COUNTER;
  private static final Counter SCHEDULED_OVERRUN_COUNTER;
  private static final Summary PERCENT_OF_PERIOD_SUMMARY;

  static {
    SUBMITTED_COUNTER = Counter.build()
        .name("scheduled_service_submitted_total")
        .labelNames("executor")
        .help("Instrumented scheduled executor service submitted total count")
        .register();
    COMPLETED_COUNTER = Counter.build()
        .name("scheduled_service_completed_total")
        .labelNames("executor")
        .help("Instrumented scheduled executor service completed total count")
        .register();

    RUNNING_GAUGE = Gauge.build()
        .name("scheduled_service_running")
        .labelNames("executor")
        .help("Instrumented scheduled executor service running total count")
        .register();

    DURATION_HISTOGRAM = Histogram.build()
        .name("scheduled_service_duration_seconds")
        .labelNames("executor")
        .help("Instrumented scheduled executor service duration (s)")
        .register();

    SCHEDULED_ONCE_COUNTER = Counter.build()
        .name("scheduled_service_scheduled_once_total")
        .labelNames("executor")
        .help("Instrumented scheduled executor service scheduled once count")
        .register();

    SCHEDULED_REPETITIVELY_COUNTER = Counter.build()
        .name("scheduled_service_scheduled_repetitively_total")
        .labelNames("executor")
        .help("Instrumented scheduled executor service scheduled repetitively count")
        .register();

    SCHEDULED_OVERRUN_COUNTER = Counter.build()
        .name("scheduled_service_scheduled_overrun_total")
        .labelNames("executor")
        .help("Instrumented scheduled executor service scheduled overrun total count")
        .register();

    PERCENT_OF_PERIOD_SUMMARY = Summary.build()
        .name("scheduled_service_period_percentage")
        .labelNames("executor")
        .help("Instrumented scheduled executor period percentage ( elapsed / period time )")
        .register();
  }

  private final ScheduledExecutorService delegate;
  private final Counter.Child submittedCounterChild;
  private final Counter.Child completedCounterChild;
  private final Gauge.Child runningGaugeChild;
  private final Histogram.Child durationHistogramChild;
  private final Counter.Child scheduledOnceCounterChild;
  private final Counter.Child scheduledRepetitivelyCounterChild;
  private final Counter.Child scheduledOverrunCounterChild;
  private final Summary.Child percentOfPeriodSummaryChild;


  public InstrumentedScheduledExecutorService(ScheduledExecutorService delegate) {
    this(delegate, "unknown_" + NAME_COUNTER.incrementAndGet());
  }

  public InstrumentedScheduledExecutorService(ScheduledExecutorService delegate, String name) {
    this.delegate = delegate;
    this.submittedCounterChild = SUBMITTED_COUNTER.labels(name);
    this.completedCounterChild = COMPLETED_COUNTER.labels(name);
    this.runningGaugeChild = RUNNING_GAUGE.labels(name);
    this.durationHistogramChild = DURATION_HISTOGRAM.labels(name);
    this.scheduledOnceCounterChild = SCHEDULED_ONCE_COUNTER.labels(name);
    this.scheduledRepetitivelyCounterChild = SCHEDULED_REPETITIVELY_COUNTER.labels(name);
    this.scheduledOverrunCounterChild = SCHEDULED_OVERRUN_COUNTER.labels(name);
    this.percentOfPeriodSummaryChild = PERCENT_OF_PERIOD_SUMMARY.labels(name);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    scheduledOnceCounterChild.inc();
    InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(command);
    return delegate.schedule(instrumentedRunnable, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    scheduledOnceCounterChild.inc();
    InstrumentedCallable<V> instrumentedCallable = new InstrumentedCallable<V>(callable);
    return delegate.schedule(instrumentedCallable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    scheduledRepetitivelyCounterChild.inc();
    InstrumentedPeriodicRunnable instrumentedPeriodicRunnable = new InstrumentedPeriodicRunnable(
        command, period, unit);
    return delegate.scheduleAtFixedRate(instrumentedPeriodicRunnable, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit) {
    scheduledRepetitivelyCounterChild.inc();
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
    submittedCounterChild.inc();
    return delegate.submit(new InstrumentedCallable<T>(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    submittedCounterChild.inc();
    return delegate.submit(new InstrumentedRunnable(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    submittedCounterChild.inc();
    return delegate.submit(new InstrumentedRunnable(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    submittedCounterChild.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAll(instrumented);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
      TimeUnit unit)
      throws InterruptedException {
    submittedCounterChild.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAll(instrumented, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    submittedCounterChild.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAny(instrumented);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    submittedCounterChild.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return delegate.invokeAny(instrumented, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    submittedCounterChild.inc();
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
      runningGaugeChild.inc();
      Histogram.Timer timer = durationHistogramChild.startTimer();
      try {
        command.run();
      } finally {
        final double elapsed = timer.observeDuration();
        runningGaugeChild.dec();
        completedCounterChild.inc();
        if (elapsed > periodInSecs) {
          scheduledOverrunCounterChild.inc();
        }
        percentOfPeriodSummaryChild.observe(elapsed / periodInSecs);
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
      runningGaugeChild.inc();
      Histogram.Timer timer = durationHistogramChild.startTimer();
      try {
        task.run();
      } finally {
        timer.observeDuration();
        runningGaugeChild.dec();
        completedCounterChild.inc();
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
      runningGaugeChild.inc();
      Histogram.Timer timer = durationHistogramChild.startTimer();
      try {
        return callable.call();
      } finally {
        timer.observeDuration();
        runningGaugeChild.dec();
        completedCounterChild.inc();
      }
    }
  }
}

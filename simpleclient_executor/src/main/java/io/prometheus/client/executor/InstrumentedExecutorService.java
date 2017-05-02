package io.prometheus.client.executor;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentedExecutorService implements ExecutorService {

  private static final AtomicLong NAME_COUNTER = new AtomicLong();
  private static final Counter ST_COUNTER;
  private static final Counter CT_COUNTER;
  private static final Gauge R_GAUGE;
  private static final Histogram D_HISTOGRAM;

  private final ExecutorService delegate;
  private final Counter.Child submittedCounter;
  private final Counter.Child completedCounter;
  private final Gauge.Child runningGauge;
  private final Histogram.Child durationHistogram;

  static {
    ST_COUNTER = Counter.build()
        .name("instrumented_executor_service_submitted_count")
        .labelNames("serviceName")
        .help("Instrumented executor service submitted total count")
        .register();
    CT_COUNTER = Counter.build()
        .name("instrumented_executor_service_completed_count")
        .labelNames("serviceName")
        .help("Instrumented executor service completed total count")
        .register();

    R_GAUGE = Gauge.build()
        .name("instrumented_executor_service_running_count")
        .labelNames("serviceName")
        .help("Instrumented executor service running total count")
        .register();

    D_HISTOGRAM = Histogram.build()
        .name("instrumented_executor_service_duration_seconds")
        .labelNames("serviceName")
        .help("Instrumented executor service duration (s)")
        .register();
  }


  public InstrumentedExecutorService(ExecutorService delegate) {
    this(delegate, "instrumented_delegate_" + NAME_COUNTER.incrementAndGet());
  }

  public InstrumentedExecutorService(ExecutorService delegate, String name) {
    this.delegate = delegate;
    this.submittedCounter = ST_COUNTER.labels(name);
    this.completedCounter = CT_COUNTER.labels(name);
    this.runningGauge = R_GAUGE.labels(name);
    this.durationHistogram = D_HISTOGRAM.labels(name);
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

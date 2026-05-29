package io.prometheus.metrics.exporter.httpserver;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
    if (!threadPoolExecutor.isShutdown()) {
      try {
        threadPoolExecutor.getQueue().put(runnable);
      } catch (InterruptedException ignored) {
        // ignore
      }
    }
  }
}

package io.prometheus.metrics.core.metrics;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental accumulator for classic-only histogram data points.
 *
 * <p>Each recording thread owns a cell with two buffers. A snapshot advances the global epoch,
 * waits only for observations that had already entered the previous epoch, and then drains the
 * inactive buffers. Recording threads therefore never contend on a shared monitor.
 *
 * <p>Cells remain registered until both buffers have been collected, after which they can be
 * reclaimed and re-registered if their recording thread is reused. A cell is static and does not
 * reference its owning accumulator, so a thread-local value cannot retain a removed or cleared data
 * point.
 */
@SuppressWarnings("ThreadLocalUsage")
final class ClassicOnlyAccumulator {

  private static final long NOT_WRITING = -1;
  // A stalled recorder must not make a scrape wait indefinitely. The next snapshot will retry
  // the buffer after the recorder has left its epoch.
  private static final long SNAPSHOT_WAIT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

  private final int bucketCount;
  private final AtomicLong epoch = new AtomicLong();
  private final Set<Cell> cells = ConcurrentHashMap.newKeySet();
  private final ThreadLocal<Cell> threadCell =
      new ThreadLocal<Cell>() {
        @Override
        protected Cell initialValue() {
          return new Cell(bucketCount);
        }
      };

  // Accessed only while holding this accumulator's monitor.
  private final long[] collectedBuckets;
  private long collectedCount;
  private double collectedSum;

  ClassicOnlyAccumulator(int bucketCount) {
    this.bucketCount = bucketCount;
    this.collectedBuckets = new long[bucketCount];
  }

  void observe(int bucket, double value) {
    Cell cell = threadCell.get();
    while (true) {
      // Cells are removed once both buffers are empty. A thread-local may outlive that removal, so
      // re-register it before every recording attempt.
      if (!cell.registered.get() || !cells.contains(cell)) {
        if (cell.registered.compareAndSet(false, true) || !cells.contains(cell)) {
          cells.add(cell);
        }
      }
      long observedEpoch = epoch.get();
      cell.writingEpoch = observedEpoch;
      // The registration check closes the race with snapshot's empty-cell reclamation. If a
      // snapshot removed this cell after the first check, do not write into an unregistered cell.
      if (!cell.registered.get() || epoch.get() != observedEpoch) {
        cell.writingEpoch = NOT_WRITING;
        continue;
      }
      try {
        CellBuffer buffer = cell.buffers[(int) (observedEpoch & 1)];
        buffer.buckets[bucket]++;
        buffer.sum += value;
        buffer.count++;
        return;
      } finally {
        // Publishes all plain writes above to a snapshot waiting on writingEpoch.
        cell.writingEpoch = NOT_WRITING;
      }
    }
  }

  @SuppressWarnings({"ModifyCollectionInEnhancedForLoop", "ThreadPriorityCheck"})
  synchronized Snapshot snapshot() {
    long inactiveEpoch = epoch.getAndIncrement();
    int inactiveBuffer = (int) (inactiveEpoch & 1);
    long waitDeadline = System.nanoTime() + SNAPSHOT_WAIT_NANOS;

    for (Cell cell : cells) {
      if (!awaitInactiveBuffer(cell, inactiveBuffer, waitDeadline)) {
        // The writer may be paused indefinitely. Leave this buffer untouched; a later snapshot
        // will collect it after the writer has published NOT_WRITING.
        continue;
      }
      CellBuffer buffer = cell.buffers[inactiveBuffer];
      for (int i = 0; i < bucketCount; i++) {
        collectedBuckets[i] += buffer.buckets[i];
        buffer.buckets[i] = 0;
      }
      collectedCount += buffer.count;
      collectedSum += buffer.sum;
      buffer.count = 0;
      buffer.sum = 0;

      // Reclaim cells from short-lived recording threads once their observations have been
      // collected. The registration check in observe makes this safe if the thread is reused.
      if (cell.writingEpoch == NOT_WRITING
          && isEmpty(cell.buffers[0])
          && isEmpty(cell.buffers[1])
          && cell.registered.compareAndSet(true, false)) {
        cells.remove(cell);
      }
    }

    return new Snapshot(collectedBuckets.clone(), collectedCount, collectedSum);
  }

  @SuppressWarnings("ThreadPriorityCheck")
  private boolean awaitInactiveBuffer(Cell cell, int inactiveBuffer, long waitDeadline) {
    while (true) {
      long writingEpoch = cell.writingEpoch;
      // An old writer can still be in the same parity after a pair of epoch flips. It is not
      // enough to compare with inactiveEpoch: draining while that writer is active would race
      // with its plain bucket writes.
      if (writingEpoch == NOT_WRITING || (writingEpoch & 1) != inactiveBuffer) {
        return true;
      }
      if (System.nanoTime() >= waitDeadline) {
        return false;
      }
      Thread.yield();
    }
  }

  private static boolean isEmpty(CellBuffer buffer) {
    return buffer.count == 0;
  }

  private static final class Cell {
    private final CellBuffer[] buffers;
    private volatile long writingEpoch = NOT_WRITING;
    private final AtomicBoolean registered = new AtomicBoolean();

    private Cell(int bucketCount) {
      buffers = new CellBuffer[] {new CellBuffer(bucketCount), new CellBuffer(bucketCount)};
    }
  }

  private static final class CellBuffer {
    private final long[] buckets;
    private long count;
    private double sum;

    private CellBuffer(int bucketCount) {
      buckets = new long[bucketCount];
    }
  }

  static final class Snapshot {
    final long[] buckets;
    final long count;
    final double sum;

    private Snapshot(long[] buckets, long count, double sum) {
      this.buckets = buckets;
      this.count = count;
      this.sum = sum;
    }
  }
}

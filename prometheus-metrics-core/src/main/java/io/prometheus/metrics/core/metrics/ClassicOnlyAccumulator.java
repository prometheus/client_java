package io.prometheus.metrics.core.metrics;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental accumulator for classic-only histogram data points.
 *
 * <p>Each recording thread owns a cell with two buffers. A snapshot advances the global epoch,
 * waits only for observations that had already entered the previous epoch, and then drains the
 * inactive buffers. Recording threads therefore never contend on a shared monitor.
 *
 * <p>Cells are retained for the lifetime of the data point so observations made by short-lived
 * threads remain available to later snapshots. A cell is static and does not reference its owning
 * accumulator, so a thread-local value cannot retain a removed or cleared data point.
 */
@SuppressWarnings("ThreadLocalUsage")
final class ClassicOnlyAccumulator {

  private static final long NOT_WRITING = -1;

  private final int bucketCount;
  private final AtomicLong epoch = new AtomicLong();
  private final ConcurrentLinkedQueue<Cell> cells = new ConcurrentLinkedQueue<>();
  private final ThreadLocal<Cell> threadCell =
      new ThreadLocal<Cell>() {
        @Override
        protected Cell initialValue() {
          Cell cell = new Cell(bucketCount);
          cells.add(cell);
          return cell;
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
      long observedEpoch = epoch.get();
      cell.writingEpoch = observedEpoch;
      if (epoch.get() != observedEpoch) {
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

  @SuppressWarnings("ThreadPriorityCheck")
  synchronized Snapshot snapshot() {
    long inactiveEpoch = epoch.getAndIncrement();
    int inactiveBuffer = (int) (inactiveEpoch & 1);

    for (Cell cell : cells) {
      while (cell.writingEpoch == inactiveEpoch) {
        Thread.yield();
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
    }

    return new Snapshot(collectedBuckets.clone(), collectedCount, collectedSum);
  }

  private static final class Cell {
    private final CellBuffer[] buffers;
    private volatile long writingEpoch = NOT_WRITING;

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

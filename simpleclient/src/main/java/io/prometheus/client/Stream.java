package io.prometheus.client;

/**
 * Created by warebot on 12/27/15.
 */

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Stream<T extends Number & Comparable<T>> {

    private static final int DEF_SAMPLE_SIZE = 4096;
    private static final Quantile[] DEF_TARGETED_QUANTILES = new Quantile[]{
            new Quantile(0.5, 0.05), new Quantile(0.99, 0.001)};

    AtomicInteger count = new AtomicInteger(0);


    private int sampleSize;

    /*
        This method also maintains the tuples of S(n)
        in a linked list. Incoming items are buffered in sorted
        order and are inserted with the aid of an insertion cursor
        which, like the compress cursor, sequentially scans
        a fraction of the tuples and inserts a buffered item whenever
        the cursor is at the appropriate position
     */
    LinkedList<Sample> samples = new LinkedList<Sample>();

    /*
        Incoming items are buffered in sorted order and are inserted/merged
        into the underlying S(n) data-structure
    */
    final ArrayList<T> buffer;
    final ArrayList<Sample> s = new ArrayList<Sample>(500);

    // Specific quantiles and their error-bounds are requested a priori
    final Quantile quantiles[];

    public Stream(final Quantile... quantiles) {
        this.quantiles = quantiles;

        sampleSize = DEF_SAMPLE_SIZE;
        buffer = new ArrayList<T>(sampleSize);
    }


    // Default Stream only targets default quantiles
    public Stream() {
        this(DEF_TARGETED_QUANTILES);
    }


    public Stream(final int sampleSize) {
        this(sampleSize, DEF_TARGETED_QUANTILES);
    }

    public Stream(final int sampleSize, final Quantile... quantiles) {
        this.quantiles = quantiles;

        this.sampleSize = sampleSize;
        buffer = new ArrayList<T>(sampleSize);
    }


    private double f(double r, int n) {

        double e = n + 1;
        double f1;

        for (Quantile quantile : quantiles) {
            if (r <= Math.floor(quantile.quantile * n)) {
                f1 = (2.0 * quantile.error * (n - r)) / (1.0 - quantile.quantile);
            } else {
                f1 = (2.0 * quantile.error * r) / quantile.quantile;
            }

            e = Math.min(e, f1);
        }
        return e;
    }


    public void insert(final T v) {
        buffer.add(buffer.size(), v);
        // compress_2();

        if (buffer.size() >= sampleSize) {
            flush();
        }
    }


    public void merge() {

        int bufferSize = buffer.size();
        if (bufferSize == 0) {
            return;
        }

        Collections.sort(buffer);

        ListIterator<Sample> iterator = samples.listIterator(0);
        int ri = 0;

        for (int bufIdx = 0; bufIdx < bufferSize; bufIdx++) {
            T v = buffer.get(bufIdx);

            Sample lower = null;
            while (iterator.hasNext()) {
                Sample vi = iterator.next();
                if (v.compareTo(vi.value) < 0) {
                    iterator.previous();
                    break;
                }
                ri += vi.g;
            }

            int delta;
            int rank = ri + bufIdx;

            if (!iterator.hasPrevious() || !iterator.hasNext()) {
                delta = 0;
            } else {
                delta = (int) Math.floor(f(rank, count.get())) - 1;
            }
            iterator.add(new Sample(v, 1, delta, ri));
            count.getAndIncrement();
        }
        if (bufferSize > sampleSize) {
            buffer.subList(sampleSize, bufferSize).clear();
            buffer.trimToSize();
        }
        buffer.clear();


    }



    private void compress() {
        if (samples.size() < 2) {
            return;
        }

        final ListIterator<Sample> it = samples.listIterator(0);

        Sample prev = null;
        Sample next = it.next();
        int ri = 0;
        int width = next.g;
        while (it.hasNext()) {
            prev = next;

            next = it.next();
            ri += width;

            if (prev.g + next.g + next.delta <= f(ri, count.get())) {
                next.g += prev.g;
                it.previous();
                it.previous();
                it.remove();
                it.next();
                width = next.g - prev.g;
            } else {
                width = next.g;
            }
        }
    }


    public void reset() {
        samples.clear();
        count.set(0);
    }

    public T query(final double quantile) throws IllegalStateException {

        // Make sure we flush any buffered items into our sample-set S(n) before we query
        flush();

        if (samples.size() == 0) {
            return null;
        }


        int ri = 0;
        final int desired = (int) (quantile * count.get());

        final ListIterator<Sample> it = samples.listIterator(0);
        Sample prev, cur;
        cur = it.next();

        while (it.hasNext()) {
            prev = cur;
            cur = it.next();
            ri += prev.g;


            if (ri + cur.g + cur.delta > desired + (f(desired, count.get())) / 2) {
                return prev.value;
            }

        }

        T v = samples.getLast().value;
        return v;
    }

    void flush() {
        // If the buffer is at capacity, merge and compress to relinquish storage.
        merge();
        compress();
    }

    private class Sample {
        final T value;
        int g;
        int rank;

        final int delta;

        public Sample(final T value, final int lowerDelta, final int delta) {
            this.value = value;
            this.g = lowerDelta;
            this.delta = delta;
        }

        public Sample(final T value, final int lowerDelta, final int delta, int rank) {
            this.value = value;
            this.g = lowerDelta;
            this.delta = delta;
            this.rank = rank;
        }

        @Override
        public String toString() {
            return String.format("%d, %d, %d", value, g, delta);
        }
    }
}
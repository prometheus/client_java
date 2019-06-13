/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Source: http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/DoubleAdder.java?revision=1.12
 */

package io.prometheus.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * One or more variables that together maintain an initially zero
 * {@code double} sum.  When updates (method {@link #add}) are
 * contended across threads, the set of variables may grow dynamically
 * to reduce contention.  Method {@link #sum} (or, equivalently {@link
 * #doubleValue}) returns the current total combined across the
 * variables maintaining the sum.
 *
 * <p>This class extends {@link Number}, but does <em>not</em> define
 * methods such as {@code equals}, {@code hashCode} and {@code
 * compareTo} because instances are expected to be mutated, and so are
 * not useful as collection keys.
 *
 * <p><em>jsr166e note: This class is targeted to be placed in
 * java.util.concurrent.atomic.</em>
 *
 * @since 1.8
 * @author Doug Lea
 */
public class DoubleAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /**
     * Update function. Note that we must use "long" for underlying
     * representations, because there is no compareAndSet for double,
     * due to the fact that the bitwise equals used in any CAS
     * implementation is not the same as double-precision equals.
     * However, we use CAS only to detect and alleviate contention,
     * for which bitwise equals works best anyway. In principle, the
     * long/double conversions used here should be essentially free on
     * most platforms since they just re-interpret bits.
     *
     * Similar conversions are used in other methods.
     */
    final long fn(long v, long x) {
        return Double.doubleToRawLongBits
                (Double.longBitsToDouble(v) +
                        Double.longBitsToDouble(x));
    }

    /**
     * Creates a new adder with initial sum of zero.
     */
    public DoubleAdder() {
    }

    /**
     * Adds the given value.
     *
     * @param x the value to add
     */
    public void add(double x) {
        Cell[] as; long b, v; int[] hc; Cell a; int n;
        if ((as = cells) != null ||
                !casBase(b = base,
                        Double.doubleToRawLongBits
                                (Double.longBitsToDouble(b) + x))) {
            boolean uncontended = true;
            if ((hc = threadHashCode.get()) == null ||
                    as == null || (n = as.length) < 1 ||
                    (a = as[(n - 1) & hc[0]]) == null ||
                    !(uncontended = a.cas(v = a.value,
                            Double.doubleToRawLongBits
                                    (Double.longBitsToDouble(v) + x))))
                retryUpdate(Double.doubleToRawLongBits(x), hc, uncontended);
        }
    }

    /**
     * Returns the current sum.  The returned value is <em>NOT</em> an
     * atomic snapshot; invocation in the absence of concurrent
     * updates returns an accurate result, but concurrent updates that
     * occur while the sum is being calculated might not be
     * incorporated.  Also, because floating-point arithmetic is not
     * strictly associative, the returned result need not be identical
     * to the value that would be obtained in a sequential series of
     * updates to a single variable.
     *
     * @return the sum
     */
    public double sum() {
        // On concurrent `sum` and `set`, it is acceptable to `get` an outdated `value`.
        // On concurrent `sum` and `add`, it is acceptable to `get` an outdated `value`.
        // On concurrent `sum` and `set` and `add`, it is possible to `get` an outdated `value`.

        // Correctness is guaranteed by `volatile` memory access ordering and visibility semantics.
        // Program order:
        //  - writes in `set` - `busy` (CAS), `cells` (Wc), `base` (Wb), `busy`
        //  - reads in `sum` - `cells` (Rc), `base` (Rb), `busy`, `cells` (Cc), `base` (Cb)
        // Note that:
        //  - `busy` is written after `cells` and `base`
        //  - `busy` is read after `cells` and `base`, then `cells` and `base` is re-read after `busy`
        // In other words:
        //  - if we see the write to `busy`, then we must see the write to `cells` and `busy` on re-read
        //  - if we don't see the write to `busy`, then we must retry as we have no guarantees
        // Execution order (in the former case):
        //  - serial
        //    - old result - Rc, Rb, Cc, Cb, Wc, Wb
        //    - new result - Wc, Wb, Rc, Rb, Cc, Cb
        //  - concurrent
        //    - old result - Rc, Wc, Rb, Wb, Cc, Cb - retry (superfluous)
        //    - new result - Wc, Rc, Wb, Rb, Cc, Cb
        //    - invalid result - Rc, Wc, Wb, Rb, Cc, Cb - retry
        //    - invalid result - Wc, Rc, Rb, Wb, Cc, Cb - retry
        Cell[] as = cells; long b = base;
        while (as != null && !(busy == 0 && cells == as && base == b)) {
            // busy waiting, retry loop
            Thread.yield();
            as = cells; b = base;
        }

        double sum = Double.longBitsToDouble(b);
        if (as != null) {
            int n = as.length;
            for (int i = 0; i < n; ++i) {
                Cell a = as[i];
                if (a != null)
                    sum += Double.longBitsToDouble(a.value);
            }
        }
        return sum;
    }

    /**
     * Resets variables maintaining the sum to zero.  This method may
     * be a useful alternative to creating a new adder, but is only
     * effective if there are no concurrent updates.  Because this
     * method is intrinsically racy, it should only be used when it is
     * known that no threads are concurrently updating.
     */
    public void reset() {
        internalReset(0L);
    }

    public void set(double x) {
        // On concurrent `set` and `set`, it should be acceptable to lose one `set` measurement.
        // On concurrent `set` and `add`, it should be acceptable to lose the `add` measurement.

        // Correctness is ensured by different techniques:
        //  - `set` waits on contention (blocking)
        //  - `add` avoids contention (non-blocking)
        //  - `sum` retries on conflicts (non-blocking)
        // Performance characteristics by use cases:
        //  - only `set` - `cells` is always `null` - no allocations
        //  - only `add` - `cells` allocated on contention
        //  - mixed `set` and `add` - `cells` allocated on contention, `cells` deallocated on `set`
        for (;;) {
            Cell[] as;
            if ((as = cells) != null) { // have cells
                if (busy == 0 && casBusy()) {
                    try {
                        if (cells == as) { // recheck under lock
                            // update cells and base (not atomic)
                            cells = null;
                            base = Double.doubleToLongBits(x);
                            break;
                        }
                    } finally {
                        busy = 0;
                    }
                }
            } else { // no cells
                // update base (atomic)
                base = Double.doubleToLongBits(x);
                break;
            }
        }
    }

    /**
     * Equivalent in effect to {@link #sum} followed by {@link
     * #reset}. This method may apply for example during quiescent
     * points between multithreaded computations.  If there are
     * updates concurrent with this method, the returned value is
     * <em>not</em> guaranteed to be the final value occurring before
     * the reset.
     *
     * @return the sum
     */
    public double sumThenReset() {
        Cell[] as = cells;
        double sum = Double.longBitsToDouble(base);
        base = 0L;
        if (as != null) {
            int n = as.length;
            for (int i = 0; i < n; ++i) {
                Cell a = as[i];
                if (a != null) {
                    long v = a.value;
                    a.value = 0L;
                    sum += Double.longBitsToDouble(v);
                }
            }
        }
        return sum;
    }

    /**
     * Returns the String representation of the {@link #sum}.
     * @return the String representation of the {@link #sum}
     */
    public String toString() {
        return Double.toString(sum());
    }

    /**
     * Equivalent to {@link #sum}.
     *
     * @return the sum
     */
    public double doubleValue() {
        return sum();
    }

    /**
     * Returns the {@link #sum} as a {@code long} after a
     * narrowing primitive conversion.
     */
    public long longValue() {
        return (long)sum();
    }

    /**
     * Returns the {@link #sum} as an {@code int} after a
     * narrowing primitive conversion.
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * Returns the {@link #sum} as a {@code float}
     * after a narrowing primitive conversion.
     */
    public float floatValue() {
        return (float)sum();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeDouble(sum());
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        busy = 0;
        cells = null;
        base = Double.doubleToRawLongBits(s.readDouble());
    }

}

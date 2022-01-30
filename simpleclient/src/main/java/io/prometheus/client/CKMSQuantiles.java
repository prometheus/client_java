package io.prometheus.client;

// Copied from https://raw.githubusercontent.com/Netflix/ocelli/master/ocelli-core/src/main/java/netflix/ocelli/stats/CKMSQuantiles.java
// Revision d0357b8bf5c17a173ce94d6b26823775b3f999f6 from Jan 21, 2015.
// Which was copied from https://github.com/umbrant/QuantileEstimation/blob/34ea6889570827a79b3f8294812d9390af23b734/src/main/java/com/umbrant/quantile/QuantileEstimationCKMS.java
//
// This is the original code except for the following modifications:
//
//  - Changed the type of the observed values from int to double.
//  - Removed the Quantiles interface and corresponding @Override annotations.
//  - Changed the package name.
//  - Make get() return NaN when no sample was observed.
//  - Make class package private

/*
 Copyright 2012 Andrew Wang (andrew@umbrant.com)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Implementation of the Cormode, Korn, Muthukrishnan, and Srivastava algorithm
 * for streaming calculation of targeted high-percentile epsilon-approximate
 * quantiles.
 * <p>
 * This is a generalization of the earlier work by Greenwald and Khanna (GK),
 * which essentially allows different error bounds on the targeted quantiles,
 * which allows for far more efficient calculation of high-percentiles.
 * <p>
 * <p>
 * See: Cormode, Korn, Muthukrishnan, and Srivastava
 * "Effective Computation of Biased Quantiles over Data Streams" in ICDE 2005
 * <p>
 * Greenwald and Khanna,
 * "Space-efficient online computation of quantile summaries" in SIGMOD 2001
 * <p>
 * <p>
 * The Approximate Quantiles Algorithm looks like this (Figure 1)
 * <pre>
 * Main()
 *    foreach item v do
 *      Insert(v);
 *      if (Compress Condition()) then
 *          Compress();
 *
 * Insert(v):
 *    r_0:=0;
 *    for i:=1 to s do
 *        ri := ri−1 + gi−1;
 *        if (v < vi) break;
 *        add (v,1,f(ri,n)−1) to S before vi;
 *    n++;
 *
 * Compress():
 *    for i := (s−1) downto 1 do
 *        if (gi + gi+1 + ∆i+1 ≤ f(ri, n)) then
 *            merge ti and ti+1;
 *
 * Output(φ):
 *    r0:=0;
 *    for i := 1 to s do
 *        ri:=ri−1+gi−1;
 *        if(ri +gi +∆i >φn+f(φn,n)/2)
 *            print(vi−1); break;
 * </pre>
 */
final class CKMSQuantiles {
    /**
     * Total number of items in stream.
     * Increases on every insertBatch().
     */
    private int count = 0;

    /**
     * Current list of sampled items, maintained in sorted order with error
     * bounds.
     * <p>
     * Note: Any algorithm that guarantees to find biased
     * quantiles φ with error at most φεn in rank must store
     * Ω(1 min{klog1/φ,log(εn)}) items.
     * </p>
     */
    final LinkedList<Item> samples;

    /**
     * Used for compress condition.
     * This is a different index than the bufferCount,
     * because flushing is also done on `quantile.get()`,
     * but we need to compress, regardless of reaching the bufferCount, to limit space usage.
     */
    private int compressIdx = 0;

    /**
     * The amount of values observed when to compress.
     */
    private final int insertThreshold;

    /**
     * Buffers incoming items to be inserted in batch.
     */
    private final double[] buffer;

    /**
     * Tracks the current index of the buffer. Increases on insert().
     */
    private int bufferIdx = 0;

    /**
     * Array of Quantiles that we care about, along with desired error.
     */
    private final Quantile[] quantiles;

    /**
     * Set up the CKMS Quantiles. Can have 0 or more targeted quantiles defined.
     * @param quantiles The targeted quantiles, can be empty.
     */
    CKMSQuantiles(Quantile[] quantiles) {
        if (quantiles.length == 0) { // we need at least one for this algorithm to work
            throw new IllegalArgumentException("quantiles cannot be empty");
        }
        this.quantiles = quantiles;

        // section 5.1 Methods - Batch.
        // This is hardcoded to 500, which corresponds to an epsilon of 0.1%.
        this.insertThreshold = 500;

        // create a buffer with size equal to threshold
        this.buffer = new double[insertThreshold];

        // Initialize empty items
        this.samples = new LinkedList<Item>();
    }

    /**
     * Add a new value from the stream.
     *
     * @param value the observed value
     */
    public void insert(double value) {
        buffer[bufferIdx] = value;
        bufferIdx++;

        if (bufferIdx == buffer.length) {
            insertBatch(); // this is the batch insert variation
        }

        // The Compress_Condition()
        compressIdx = (compressIdx + 1) % insertThreshold;
        if (compressIdx == 0) {
            compress();
        }
    }

    /**
     * Get the estimated value at the specified quantile.
     *
     * @param q Queried quantile, e.g. 0.50 or 0.99.
     * @return Estimated value at that quantile.
     */
    public double get(double q) {
        // clear the buffer. in case of low value insertions, the samples can become stale.
        // On every get, make sure we get the latest values in.
        insertBatch();

        if (samples.size() == 0) {
            return Double.NaN;
        }

        // Straightforward implementation of Output(q).
        // Paper Section 3.1 on true rank: let r_i = Sum_{j=1}^{i−1} g_j
        int currentRank = 0;
        double desired = q * count;

        ListIterator<Item> it = samples.listIterator();
        Item prev, cur;
        cur = it.next();
        while (it.hasNext()) {
            prev = cur;
            cur = it.next();

            currentRank += prev.g;

            if (currentRank + cur.g + cur.delta > desired
                    + (allowableError(desired) / 2)) {
                return prev.value;
            }
        }

        // edge case of wanting max value
        return samples.getLast().value;
    }

    /**
     * Specifies the allowable error for this rank, depending on which quantiles
     * are being targeted.
     * <p>
     * This is the f(r_i, n) function from the CKMS paper. It's basically how
     * wide the range of this rank can be.
     * <p>
     * Define invariant function f (ri , n) as
     * (i) f_j(r_i,n) = 2ε_j r_i / φ_j for φ_j n ≤ r_i ≤ n;
     * (ii) f_j(r_i,n) = 2ε_j(n−r_i) / (1−φ_j) for  0 ≤ r_i ≤ φ_j n
     * <p>
     * and take f(ri,n) = max{min_j ⌊f_j(r_i,n)⌋,1}.
     * As before we ensure that for all i, g_i + ∆_i ≤ f(r_i, n).
     *
     * @param rank the index in the list of samples
     */
    private double allowableError(double rank /* r_i */) {
        int n = count;
        double minError = count;

        for (Quantile q : quantiles) {
            double error;
            if (rank <= q.quantile * n) {
                error = q.u * (n - rank);
            } else {
                error = q.v * rank;
            }
            if (error < minError) {
                minError = error;
            }
        }
        return Math.max(minError, 1);
    }

    /**
     * To insert a new item, v, we find i such that vi < v ≤ vi+1,
     * we compute ri and insert the tuple (v,g=1,∆=f(ri,n)−1).
     *
     * We also ensure that min and max are kept exactly, so when v < v1,
     * we insert the tuple (v,g = 1,∆ = 0) before v1. Similarly, when v > vs,
     * we insert (v,g = 1,∆ = 0) after vs.
     */
    private void insertBatch() {
        if (bufferIdx == 0) {
            return;
        }
        // Has to be sorted: O(buffer)
        // Since the buffer is treated as a circular buffer, we sort till the bufferIdx to prevent insertion of duplicate / already inserted values.
        Arrays.sort(buffer, 0, bufferIdx);

        // Base case: no samples yet
        int start = 0;
        if (samples.size() == 0) {
            Item newItem = new Item(buffer[0], 0);
            samples.add(newItem);
            start++;
            count++;
        }

        // To insert a new item, v, we find i such that vi < v ≤ vi+1,
        ListIterator<Item> it = samples.listIterator();
        Item item = it.next();

        // Keep track of the current rank by adding the g of each item. See also discussion in https://issues.apache.org/jira/browse/HBASE-14324
        // Paper Section 3.1 on true rank: let r_i = Sum_{j=1}^{i−1} g_j
        int currentRank = item.g;

        for (int i = start; i < bufferIdx; i++) {
            // item to be inserted
            double v = buffer[i];
            // find the item in the samples that is bigger than our v.
            while (it.hasNext() && item.value < v) {
                item = it.next();
                currentRank += item.g;
            }

            // If we found that bigger item, back up so we insert ourselves
            // before it
            if (item.value > v) {
                currentRank -= item.g;
                it.previous();
            }

            // We use different indexes for the edge comparisons, because of the
            // above if statement that adjusts the iterator
            int delta;
            if (it.previousIndex() == 0 || it.nextIndex() == samples.size()) {
                delta = 0;
            } else {
                delta = ((int) Math.floor(allowableError(currentRank))) - 1;
            }

            Item newItem = new Item(v, delta);
            it.add(newItem);
            count++;
            item = newItem;
        }

        // reset buffered items to 0.
        bufferIdx = 0;
    }

    /**
     * Try to remove extraneous items from the set of sampled items. This checks
     * if an item is unnecessary based on the desired error bounds, and merges
     * it with the adjacent item if it is.
     * <p>
     * Compress. Periodically, the algorithm scans the data
     * structure and merges adjacent nodes when this does not
     * violate the invariant. That is, remove nodes (vi, gi, ∆i)
     * and (vi+1 , gi+1 , ∆i+1 ), and replace with (vi+1 , (gi +
     * gi+1 ), ∆i+1 ) provided that (gi + gi+1 + ∆i+1 ) ≤ f (ri , n).
     * This also maintains the semantics of g and ∆ being the
     * difference in rank between v_i and v_{i-1} , and the difference
     * between the highest and lowest possible ranks of vi, respectively.
     */
    private void compress() {
        // If there are 0,1, or 2 samples then there's nothing to compress.
        if (samples.size() < 3) {
            return;
        }

        ListIterator<Item> it = samples.listIterator();

        Item prev;
        Item next = it.next();

        // Counter for the rank in the stream of all observed values.
        // Paper Section 3.1 on true rank: let r_i = Sum_{j=1}^{i−1} g_j
        int currentRank = next.g;

        while (it.hasNext()) {
            prev = next;
            next = it.next();
            currentRank += next.g;
            if (prev.g + next.g + next.delta <= allowableError(currentRank)) {
                next.g += prev.g;
                // Remove prev. it.remove() kills the last thing returned.
                it.previous(); // brings pointer back to 'next'
                it.previous(); // brings pointer back to 'prev'
                it.remove(); // remove prev
                // it.next() is now equal to next,
                it.next(); // set pointer to 'next'
            }
        }
    }

    /**
     * As in GK, the data structure at time n, S(n), consists
     * of a sequence of s tuples ⟨ti = (vi, gi, ∆i)⟩, where each vi
     * is a sampled item from the data stream and two additional
     * values are kept: (1) g_i is the difference between the lowest
     * possible rank of item i and the lowest possible rank of item
     * i − 1; and (2) ∆_i is the difference between the greatest
     * possible rank of item i and the lowest possible rank of item
     * i.
     */
    private static class Item {
        /**
         * vi
         * is a sampled item from the data stream and two additional
         * values are kept
         */
        final double value;
        /**
         * g_i is the difference between the lowest
         * possible rank of item i and the lowest possible rank of item
         * i − 1.
         * note: Always starts with 1, changes when merging Items.
         */
        int g = 1;
        /**
         * ∆i is the difference between the greatest
         * possible rank of item i and the lowest possible rank of item
         * i.
         */
        final int delta;

        Item(double value, int delta) {
            this.value = value;
            this.delta = delta;
        }

        @Override
        public String toString() {
            return String.format("I{val=%.3f, g=%d, del=%d}", value, g, delta);
        }
    }

    /**
     *
     */
    static class Quantile {
        /**
         * 0 < φ < 1
         */
        final double quantile;
        /**
         * Allowed error 0 < ε < 1
         */
        final double epsilon;
        /**
         * Helper value to calculate the targeted quantiles invariant as per Definition 5 (ii)
         */
        final double u;
        /**
         * Helper value to calculate the targeted quantiles invariant as per Definition 5 (i)
         */
        final double v;

        /**
         * Targeted quantile: T = {(φ_j , ε_j )}
         * Rather than requesting the same ε for all quantiles (the uniform case)
         * or ε scaled by φ (the biased case), one might specify an arbitrary set
         * of quantiles and the desired errors of ε for each in the form (φj , εj ).
         * For example, input to the targeted quantiles problem might be {(0.5, 0.1), (0.2, 0.05), (0.9, 0.01)},
         * meaning that the median should be returned with 10% error, the 20th percentile with 5% error,
         * and the 90th percentile with 1%.
         *
         * @param quantile the quantile between 0 and 1
         * @param epsilon  the desired error for this quantile, between 0 and 1.
         */
        Quantile(double quantile, double epsilon) {
            if (quantile <= 0 || quantile >= 1.0) throw new IllegalArgumentException("Quantile must be between 0 and 1");
            if (epsilon <= 0 || epsilon >= 1.0) throw new IllegalArgumentException("Epsilon must be between 0 and 1");

            this.quantile = quantile;
            this.epsilon = epsilon;
            //  f_j(r_i,n) = 2ε_j(n−r_i) / (1−φ_j) for  0 ≤ r_i ≤ φ_j n
            u = 2.0 * epsilon / (1.0 - quantile);
            // f_j(r_i,n) = 2ε_j r_i / φ_j for φ_j n ≤ r_i ≤ n;
            v = 2.0 * epsilon / quantile;
        }

        @Override
        public String toString() {
            return String.format("Q{q=%.3f, eps=%.3f}", quantile, epsilon);
        }
    }

}

package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class FixedBuckets implements Iterable<FixedBucket> {

    private final List<FixedBucket> buckets;

    private FixedBuckets(List<FixedBucket> buckets) {
        buckets = new ArrayList<>(buckets);
        Collections.sort(buckets);
        this.buckets = Collections.unmodifiableList(buckets);
        if (buckets.size() == 0 || buckets.get(buckets.size()-1).getUpperBound() != Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Buckets must contain the +Inf bucket.");
        }
        for (int i=1; i<buckets.size(); i++) {
            FixedBucket prev = buckets.get(i-1);
            FixedBucket next = buckets.get(i);
            if (prev.getUpperBound() == next.getUpperBound()) {
                throw new IllegalArgumentException("Duplicate upper bound " + buckets.get(i).getUpperBound());
            }
            if (prev.getCumulativeCount() > next.getCumulativeCount()) {
                throw new IllegalArgumentException("Bucket counts must be cumulative.");
            }
        }
    }

    public static FixedBuckets of(List<FixedBucket> buckets) {
        return new FixedBuckets(buckets);
    }

    public static FixedBuckets of(FixedBucket... buckets) {
        return of(Arrays.asList(buckets));
    }

    @Override
    public Iterator<FixedBucket> iterator() {
        return buckets.iterator();
    }

    public Stream<FixedBucket> stream() {
        return buckets.stream();
    }
}

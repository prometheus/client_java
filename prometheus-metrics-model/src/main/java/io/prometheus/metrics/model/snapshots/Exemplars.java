package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable container for Exemplars.
 * <p>
 * This is currently backed by a {@code List<Exemplar>}. May be refactored later to use a more efficient data structure.
 */
public class Exemplars implements Iterable<Exemplar> {

    /**
     * EMPTY means no Exemplars.
     */
    public static final Exemplars EMPTY = new Exemplars(Collections.emptyList());
    private final List<Exemplar> exemplars;

    private Exemplars(Collection<Exemplar> exemplars) {
        ArrayList<Exemplar> copy = new ArrayList<>(exemplars.size());
        for (Exemplar exemplar : exemplars) {
            if (exemplar == null) {
                throw new NullPointerException("Illegal null value in Exemplars");
            }
            copy.add(exemplar);
        }
        this.exemplars = Collections.unmodifiableList(copy);
    }

    /**
     * Create a new Exemplars instance.
     * You can either create Exemplars with one of the static {@code Exemplars.of(...)} methods,
     * or you can use the {@link Exemplars#builder()}.
     *
     * @param exemplars a copy of the exemplars collection will be created.
     */
    public static Exemplars of(Collection<Exemplar> exemplars) {
        return new Exemplars(exemplars);
    }

    /**
     * Create a new Exemplars instance.
     * You can either create Exemplars with one of the static {@code Exemplars.of(...)} methods,
     * or you can use the {@link Exemplars#builder()}.
     *
     * @param exemplars a copy of the exemplars array will be created.
     */
    public static Exemplars of(Exemplar... exemplars) {
        return new Exemplars(Arrays.asList(exemplars));
    }

    @Override
    public Iterator<Exemplar> iterator() {
        return exemplars.iterator();
    }

    public int size() {
        return exemplars.size();
    }

    public Exemplar get(int index) {
        return exemplars.get(index);
    }

    /**
     * This is used by classic histograms to find an exemplar with a value between lowerBound and upperBound.
     * If there is more than one exemplar within the bounds the one with the newest time stamp is returned.
     */
    public Exemplar get(double lowerBound, double upperBound) {
        Exemplar result = null;
        for (int i = 0; i < exemplars.size(); i++) {
            Exemplar exemplar = exemplars.get(i);
            double value = exemplar.getValue();
            if (value > lowerBound && value <= upperBound) {
                if (result == null) {
                    result = exemplar;
                } else if (result.hasTimestamp() && exemplar.hasTimestamp()) {
                   if (exemplar.getTimestampMillis() > result.getTimestampMillis()) {
                       result = exemplar;
                   }
                }
            }
        }
        return result;
    }

    /**
     * Find the Exemplar with the newest timestamp. May return {@code null}.
     */
    public Exemplar getLatest() {
        Exemplar latest = null;
        for (int i=0; i<exemplars.size(); i++) {
            Exemplar candidate = exemplars.get(i);
            if (candidate == null) {
                continue;
            }
            if (latest == null) {
                latest = candidate;
                continue;
            }
            if (!latest.hasTimestamp()) {
                latest = candidate;
                continue;
            }
            if (candidate.hasTimestamp()) {
                if (latest.getTimestampMillis() < candidate.getTimestampMillis()) {
                    latest = candidate;
                }
            }
        }
        return latest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ArrayList<Exemplar> exemplars = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add an exemplar. This can be called multiple times to add multiple exemplars.
         */
        public Builder exemplar(Exemplar exemplar) {
            exemplars.add(exemplar);
            return this;
        }

        /**
         * Add all exemplars form the collection.
         */
        public Builder exemplars(Collection<Exemplar> exemplars) {
            this.exemplars.addAll(exemplars);
            return this;
        }

        public Exemplars build() {
            return Exemplars.of(exemplars);
        }
    }
}

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
        this.exemplars = Collections.unmodifiableList(new ArrayList<>(exemplars));
    }

    /**
     * Create a new Exemplars instance.
     * You can either create Exemplars with one of the static {@code Exemplars.of(...)} methods,
     * or you can use the {@link Exemplars#newBuilder()}.
     *
     * @param exemplars a copy of the exemplars collection will be created.
     */
    public static Exemplars of(Collection<Exemplar> exemplars) {
        return new Exemplars(exemplars);
    }

    /**
     * Create a new Exemplars instance.
     * You can either create Exemplars with one of the static {@code Exemplars.of(...)} methods,
     * or you can use the {@link Exemplars#newBuilder()}.
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
     */
    public Exemplar get(double lowerBound, double upperBound) {
        for (int i = 0; i < exemplars.size(); i++) {
            Exemplar exemplar = exemplars.get(i);
            double value = exemplar.getValue();
            if (value > lowerBound && value <= upperBound) {
                return exemplar;
            }
        }
        return null;
    }

    public static class Builder {

        private final ArrayList<Exemplar> exemplars = new ArrayList<>();

        private Builder() {
        }

        public Builder addExemplar(Exemplar exemplar) {
            exemplars.add(exemplar);
            return this;
        }

        public Builder addExemplars(List<Exemplar> exemplars) {
            this.exemplars.addAll(exemplars);
            return this;
        }

        public Exemplars build() {
            return Exemplars.of(exemplars);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}

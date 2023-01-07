package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Exemplars implements Iterable<Exemplar> {

    public static final Exemplars EMPTY = new Exemplars(Collections.emptyList());
    private final List<Exemplar> exemplars;

    private Exemplars(Collection<Exemplar> exemplars) {
        this.exemplars = new ArrayList<>(exemplars);
    }

    public static Exemplars of(Collection<Exemplar> exemplars) {
        return new Exemplars(exemplars);
    }

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
}

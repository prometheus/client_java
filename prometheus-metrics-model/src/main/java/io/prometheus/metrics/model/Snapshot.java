package io.prometheus.metrics.model;

/**
 * Consistent snapshot for reading.
 * If the data is consistent anyway (like a Counter), you can just use this.
 * If the data has state that might become inconsistent during update, create an actual snapshot.
 * Clients will not attempt to modify the snapshot
 */
public abstract class Snapshot {
    public abstract Labels getLabels();
}

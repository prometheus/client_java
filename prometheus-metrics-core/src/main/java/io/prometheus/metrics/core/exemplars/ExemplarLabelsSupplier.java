package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Global holder for a {@link Supplier} of additional {@link Labels} that are merged into every
 * automatically-sampled Exemplar across the entire application.
 *
 * <p>This is the global counterpart to the per-metric {@code exemplarLabelsSupplier(...)} builder
 * method. Registering a supplier here affects <em>all</em> metrics, including metrics registered by
 * third-party libraries that the application does not control. This makes it the right tool when
 * you cannot modify the code that creates the metrics.
 *
 * <p>The supplier is invoked on the metric hot path (rate-limited by the exemplar sampler), each
 * time an Exemplar is sampled from a valid, sampled span context. It should therefore be cheap and
 * non-blocking. It may return dynamic, request-scoped values, for example an identifier read from a
 * thread-local:
 *
 * <pre>{@code
 * ExemplarLabelsSupplier.setExemplarLabelsSupplier(
 *     () -> Labels.of("management_id", currentManagementId()));
 * }</pre>
 *
 * <p>Labels returned by the supplier that collide with {@code trace_id}/{@code span_id} (or, when a
 * per-metric supplier is also configured, with that supplier's labels) are silently dropped rather
 * than causing an error: the per-metric supplier takes precedence over the global one, and the
 * reserved {@code trace_id}/{@code span_id} labels always win. If the supplier throws, the
 * exception is swallowed and the Exemplar is created without the additional labels, so a
 * misbehaving supplier never breaks metric collection.
 */
@StableApi
public class ExemplarLabelsSupplier {

  private static final AtomicReference<Supplier<Labels>> supplierRef = new AtomicReference<>();

  private ExemplarLabelsSupplier() {}

  /**
   * Register a global supplier of additional exemplar labels. Pass {@code null} to remove a
   * previously registered supplier. The most recently registered supplier wins.
   */
  public static void setExemplarLabelsSupplier(@Nullable Supplier<Labels> supplier) {
    supplierRef.set(supplier);
  }

  /** Returns the registered global supplier, or {@code null} if none has been set. */
  @Nullable
  public static Supplier<Labels> getExemplarLabelsSupplier() {
    return supplierRef.get();
  }
}

# prometheus-metrics-model

```
instrumentation library -> metrics model -> exposition formats
```

The metrics model implements immutable, read-only Java classes that are produced by the instrumentation
library when metrics are collected.

The model is for instrumentation libraries who want to expose their metrics in Prometheus format.
If you want to instrument an application, don't use `prometheus-metrics-model` directly.
Use `prometheus-metrics-core` instead.

## Validation and Exceptions

A metrics library should not throw Exceptions. For an application developer it should be safe to add metrics
to their application, and they should not need to worry about unexpected runtime exceptions if they are using
the metrics library wrong. Worst case, metrics should be missing.

However, `prometheus-metrics-model` is _not_ a metrics library. It's a data model to be produced by metrics libraries.

* A data model should store data as-is. It should not implement implicit functionality. For example, it should not
  implicitly strip unsupported characters from metric names, and it should not implicitly add the +Inf bucket to
  histograms. A metric library can do these things, a data model should not.
* The data stored in `prometheus-metrics-model` should be valid. The data is to be used by libraries implementing
  exposition formats. These libraries expect that data is valid.

So, in order to implement these two properties, `prometheus-metrics-model` throws Exceptions
(`IllegalArgumentException`, `NullPointerException`) if snapshots with invalid data are created.
It's up to the metric library producing the data to make sure that data is valid so that these Exceptions aren't thrown.
The validation rules (metric names, mandatory parameters, etc.) are documented in the Javadoc.

If a snapshot can be created without Exception it is guaranteed to contain valid data.

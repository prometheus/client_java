# prometheus-metrics-model

```
instrumentation library -> metrics model -> exposition formats
```

The metrics model implements immutable, read-only Java classes that are produced by the instrumentation
library when metrics are collected.

Typically you don't need to use metrics-model directly. You instrument your application with an instrumentation
library, and the library takes care to produce the metrics model under the hood.

The metrics library provided by the Prometheus community is `prometheus-metrics-core`. However, the metrics model
is intended to be used by 3rd party metrics libraries as well to allow them easy Prometheus support.

## 

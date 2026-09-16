# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-16T04:08:07Z
- **Commit:** [`59ca1f0`](https://github.com/prometheus/client_java/commit/59ca1f0de4637fe8f4c72baac6910b85c0e87f5d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 709.34M | ± 16067.43K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 213.67M | ± 4976.85K | ops/s |
| prometheusLabelValuesInc | 141.83M | ± 1499.64K | ops/s |
| prometheusLabelValuesIncSingleThread | 73.51M | ± 1978.85K | ops/s |
| prometheusInc | 68.16K | ± 1.39K | ops/s |
| prometheusNoLabelsInc | 62.31K | ± 1.39K | ops/s |
| codahaleIncNoLabels | 59.72K | ± 1.37K | ops/s |
| prometheusAdd | 59.17K | ± 2.62K | ops/s |
| openTelemetryIncNoLabels | 30.88K | ± 349.66 | ops/s |
| openTelemetryInc | 25.80K | ± 961.16 | ops/s |
| openTelemetryAdd | 21.88K | ± 236.04 | ops/s |
| simpleclientNoLabelsInc | 11.11K | ± 169.68 | ops/s |
| simpleclientInc | 10.84K | ± 130.81 | ops/s |
| simpleclientAdd | 10.49K | ± 508.26 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.29K | ± 151.76 | ops/s |
| prometheusClassic | 9.81K | ± 406.99 | ops/s |
| simpleclient | 7.03K | ± 69.52 | ops/s |
| prometheusNative | 4.53K | ± 360.62 | ops/s |
| prometheusClassicSingleThread | 4.47K | ± 57.44 | ops/s |
| openTelemetryClassic | 1.04K | ± 35.61 | ops/s |
| openTelemetryExponential | 989.17 | ± 104.98 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 33.25K | ± 510.35 | ops/s |
| prometheusWriteToNull | 32.48K | ± 1.35K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 841.37K | ± 10.08K | ops/s |
| prometheusWriteToNull | 799.01K | ± 34.42K | ops/s |
| openMetricsWriteToByteArray | 765.38K | ± 36.67K | ops/s |
| openMetricsWriteToNull | 751.34K | ± 28.90K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.043 | — | — |
| CounterBenchmark.openTelemetryInc | 0.036 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.030 | — | — |
| CounterBenchmark.prometheusAdd | 0.062 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.054 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 64.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 64.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.059 | — | — |
| CounterBenchmark.simpleclientAdd | 0.089 | — | — |
| CounterBenchmark.simpleclientInc | 0.086 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.084 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.910 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.956 | — | — |
| HistogramBenchmark.prometheusClassic | 0.377 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.532 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.623 | — | — |
| HistogramBenchmark.prometheusNative | 417712.839 | — | — |
| HistogramBenchmark.simpleclient | 0.135 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.105 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.108 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      59723.639   ± 1367.238  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      21881.045    ± 236.044  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      25795.648    ± 961.160  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      30877.053    ± 349.659  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      59166.728   ± 2623.519  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  709341262.593 ± 16067434.999  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  213669674.178 ± 4976845.337  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      68163.668   ± 1393.039  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  141826389.983 ± 1499643.324  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   73514165.441 ± 1978851.561  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      62306.306   ± 1392.279  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10493.417    ± 508.261  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10838.458    ± 130.808  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      11105.044    ± 169.681  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       1042.984     ± 35.610  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        989.167    ± 104.981  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9807.048    ± 406.987  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15291.480    ± 151.761  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4468.979     ± 57.436  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4525.454    ± 360.617  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       7027.758     ± 69.519  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33246.131    ± 510.349  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      32478.820   ± 1346.019  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     765382.097  ± 36668.585  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     751341.094  ± 28901.478  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     841374.018  ± 10083.335  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     799010.517  ± 34419.215  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter updates and label-value lookup (selected methods only) |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

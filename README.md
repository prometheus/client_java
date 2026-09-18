# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-18T04:09:28Z
- **Commit:** [`f1bd53a`](https://github.com/prometheus/client_java/commit/f1bd53a3fe773529f8129e7088caf5051759be82)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 294.52M | ± 2027.21K | ops/s |
| prometheusLabelValuesInc | 146.92M | ± 910.07K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 110.83M | ± 35.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 72.11M | ± 149.37K | ops/s |
| prometheusInc | 31.42K | ± 1.05K | ops/s |
| codahaleIncNoLabels | 31.40K | ± 1.48K | ops/s |
| prometheusNoLabelsInc | 30.93K | ± 263.20 | ops/s |
| prometheusAdd | 30.46K | ± 385.09 | ops/s |
| openTelemetryIncNoLabels | 19.99K | ± 32.77 | ops/s |
| openTelemetryInc | 17.48K | ± 300.07 | ops/s |
| openTelemetryAdd | 15.41K | ± 202.78 | ops/s |
| simpleclientAdd | 7.85K | ± 35.21 | ops/s |
| simpleclientInc | 7.84K | ± 39.62 | ops/s |
| simpleclientNoLabelsInc | 7.72K | ± 24.10 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.94K | ± 11.90 | ops/s |
| simpleclient | 5.04K | ± 21.71 | ops/s |
| prometheusClassic | 4.76K | ± 2.21K | ops/s |
| prometheusClassicSingleThread | 3.40K | ± 87.35 | ops/s |
| prometheusNative | 2.05K | ± 325.59 | ops/s |
| openTelemetryExponential | 461.45 | ± 44.14 | ops/s |
| openTelemetryClassic | 458.29 | ± 30.48 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 21.19K | ± 69.64 | ops/s |
| openMetricsWriteToNull | 21.19K | ± 89.41 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 370.53K | ± 4.99K | ops/s |
| prometheusWriteToByteArray | 369.18K | ± 7.49K | ops/s |
| openMetricsWriteToByteArray | 344.79K | ± 2.51K | ops/s |
| openMetricsWriteToNull | 343.39K | ± 8.58K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.030 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.061 | — | — |
| CounterBenchmark.openTelemetryInc | 0.054 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.046 | — | — |
| CounterBenchmark.prometheusAdd | 0.121 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.117 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.119 | — | — |
| CounterBenchmark.simpleclientAdd | 0.119 | — | — |
| CounterBenchmark.simpleclientInc | 0.119 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.121 | — | — |
| HistogramBenchmark.openTelemetryClassic | 2.050 | — | — |
| HistogramBenchmark.openTelemetryExponential | 2.043 | — | — |
| HistogramBenchmark.prometheusClassic | 0.910 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.026 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.823 | — | — |
| HistogramBenchmark.prometheusNative | 417713.858 | — | — |
| HistogramBenchmark.simpleclient | 0.186 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.165 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.165 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18429.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.002 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      31398.017   ± 1478.520  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15405.074    ± 202.784  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17482.139    ± 300.069  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      19989.005     ± 32.771  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      30461.848    ± 385.090  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  294519804.217 ± 2027214.328  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  110828436.377  ± 35751.329  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31419.941   ± 1051.353  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  146918370.325 ± 910065.119  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   72105621.043 ± 149372.998  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30933.115    ± 263.204  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7854.696     ± 35.213  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7843.454     ± 39.625  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7721.873     ± 24.096  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        458.292     ± 30.485  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        461.454     ± 44.137  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4757.347   ± 2208.727  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7938.959     ± 11.895  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3395.247     ± 87.352  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2046.325    ± 325.587  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5040.690     ± 21.707  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      21187.089     ± 89.414  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      21189.004     ± 69.638  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     344788.238   ± 2512.198  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     343389.634   ± 8581.062  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     369175.352   ± 7489.019  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     370530.626   ± 4986.441  ops/s
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

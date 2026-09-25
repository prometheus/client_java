# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-25T04:24:07Z
- **Commit:** [`cce26b8`](https://github.com/prometheus/client_java/commit/cce26b87ccc0d8dbb83e8532ce7adba8f26c5372)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 524.67M | ± 5304.68K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 287.63M | ± 228.26K | ops/s |
| prometheusLabelValuesInc | 109.04M | ± 632.64K | ops/s |
| prometheusLabelValuesIncSingleThread | 53.07M | ± 120.81K | ops/s |
| prometheusInc | 57.49K | ± 3.18K | ops/s |
| prometheusNoLabelsInc | 52.12K | ± 477.41 | ops/s |
| prometheusAdd | 45.69K | ± 4.27K | ops/s |
| codahaleIncNoLabels | 44.10K | ± 346.92 | ops/s |
| openTelemetryBoundInc | 34.79K | ± 480.96 | ops/s |
| openTelemetryBoundAdd | 29.65K | ± 1.44K | ops/s |
| openTelemetryIncNoLabels | 21.04K | ± 195.17 | ops/s |
| openTelemetryInc | 16.64K | ± 24.07 | ops/s |
| openTelemetryAdd | 14.64K | ± 103.58 | ops/s |
| simpleclientInc | 6.12K | ± 60.74 | ops/s |
| simpleclientNoLabelsInc | 5.99K | ± 194.28 | ops/s |
| simpleclientAdd | 5.90K | ± 232.16 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.63K | ± 130.39 | ops/s |
| prometheusClassic | 6.80K | ± 499.55 | ops/s |
| openTelemetryBoundClassic | 6.60K | ± 1.05K | ops/s |
| prometheusClassicSingleThread | 5.48K | ± 250.09 | ops/s |
| openTelemetryClassic | 4.58K | ± 367.59 | ops/s |
| simpleclient | 4.56K | ± 96.54 | ops/s |
| prometheusNative | 2.96K | ± 108.67 | ops/s |
| openTelemetryBoundExponential | 711.59 | ± 37.62 | ops/s |
| openTelemetryExponential | 633.33 | ± 44.30 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.78K | ± 143.64 | ops/s |
| openMetricsWriteToNull | 27.05K | ± 397.14 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 672.05K | ± 2.05K | ops/s |
| prometheusWriteToByteArray | 660.14K | ± 7.37K | ops/s |
| openMetricsWriteToNull | 623.41K | ± 5.06K | ops/s |
| openMetricsWriteToByteArray | 608.36K | ± 6.07K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.021 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.063 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.031 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryInc | 0.056 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.044 | — | — |
| CounterBenchmark.prometheusAdd | 0.081 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.064 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.071 | — | — |
| CounterBenchmark.simpleclientAdd | 0.158 | — | — |
| CounterBenchmark.simpleclientInc | 0.153 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.156 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.145 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.325 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.206 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.481 | — | — |
| HistogramBenchmark.prometheusClassic | 0.545 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.644 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.532 | — | — |
| HistogramBenchmark.prometheusNative | 417713.252 | — | — |
| HistogramBenchmark.simpleclient | 0.204 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.129 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.126 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18429.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44101.882    ± 346.920  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      14642.355    ± 103.579  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      29646.586   ± 1442.469  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      34789.544    ± 480.955  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      16643.328     ± 24.065  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21037.363    ± 195.174  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      45692.751   ± 4269.843  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  524666025.401 ± 5304675.779  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  287626203.950 ± 228264.482  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57485.954   ± 3179.983  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  109043600.535 ± 632643.030  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   53069725.258 ± 120806.638  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52119.120    ± 477.407  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5895.568    ± 232.164  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6118.998     ± 60.743  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5993.058    ± 194.281  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6599.166   ± 1045.888  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        711.586     ± 37.622  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4580.468    ± 367.592  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        633.331     ± 44.304  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6801.523    ± 499.554  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12630.982    ± 130.392  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5484.557    ± 250.092  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2955.789    ± 108.670  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4564.995     ± 96.544  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27050.844    ± 397.139  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27776.165    ± 143.641  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     608363.622   ± 6073.109  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     623411.673   ± 5057.401  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     660137.390   ± 7365.306  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     672050.505   ± 2051.227  ops/s
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

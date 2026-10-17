# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-17T04:21:55Z
- **Commit:** [`eadfb6d`](https://github.com/prometheus/client_java/commit/eadfb6d550809340c24ed2a78b33ce30587a0364)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 732.25M | ± 7652.91K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 219.29M | ± 5024.95K | ops/s |
| prometheusLabelValuesInc | 207.05M | ± 5020.85K | ops/s |
| prometheusLabelValuesIncSingleThread | 94.32M | ± 1640.37K | ops/s |
| prometheusInc | 68.37K | ± 1.11K | ops/s |
| prometheusNoLabelsInc | 64.59K | ± 1.06K | ops/s |
| codahaleIncNoLabels | 61.61K | ± 2.93K | ops/s |
| prometheusAdd | 58.61K | ± 2.50K | ops/s |
| openTelemetryBoundInc | 52.38K | ± 595.06 | ops/s |
| openTelemetryBoundAdd | 48.01K | ± 838.40 | ops/s |
| openTelemetryIncNoLabels | 35.27K | ± 540.06 | ops/s |
| openTelemetryInc | 35.11K | ± 253.48 | ops/s |
| openTelemetryAdd | 28.49K | ± 1.61K | ops/s |
| simpleclientNoLabelsInc | 10.67K | ± 116.54 | ops/s |
| simpleclientInc | 10.64K | ± 314.89 | ops/s |
| simpleclientAdd | 10.36K | ± 153.31 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.19K | ± 232.53 | ops/s |
| prometheusClassic | 11.85K | ± 1.79K | ops/s |
| simpleclient | 6.97K | ± 119.51 | ops/s |
| openTelemetryBoundClassic | 6.51K | ± 1.79K | ops/s |
| openTelemetryClassic | 6.13K | ± 1.41K | ops/s |
| prometheusNative | 4.55K | ± 432.74 | ops/s |
| prometheusClassicSingleThread | 4.49K | ± 60.09 | ops/s |
| openTelemetryBoundExponential | 1.07K | ± 70.45 | ops/s |
| openTelemetryExponential | 1.00K | ± 75.53 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 36.23K | ± 441.90 | ops/s |
| prometheusWriteToNull | 35.73K | ± 486.06 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 801.96K | ± 23.78K | ops/s |
| prometheusWriteToByteArray | 800.81K | ± 40.47K | ops/s |
| openMetricsWriteToByteArray | 771.93K | ± 29.81K | ops/s |
| openMetricsWriteToNull | 741.66K | ± 37.35K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.033 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.018 | — | — |
| CounterBenchmark.openTelemetryInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.027 | — | — |
| CounterBenchmark.prometheusAdd | 0.063 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.054 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.057 | — | — |
| CounterBenchmark.simpleclientAdd | 0.091 | — | — |
| CounterBenchmark.simpleclientInc | 0.089 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.088 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.155 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.883 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.162 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.951 | — | — |
| HistogramBenchmark.prometheusClassic | 0.320 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.548 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.623 | — | — |
| HistogramBenchmark.prometheusNative | 253872.862 | — | — |
| HistogramBenchmark.simpleclient | 0.136 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.097 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.099 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      61608.827   ± 2930.427  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      28491.415   ± 1610.419  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      48007.181    ± 838.403  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      52384.598    ± 595.065  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      35110.223    ± 253.477  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      35270.237    ± 540.055  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      58613.889   ± 2495.401  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  732253456.781 ± 7652911.073  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  219285297.668 ± 5024949.515  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      68370.484   ± 1111.894  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  207052236.461 ± 5020846.476  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   94316091.863 ± 1640365.372  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      64593.430   ± 1059.447  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10359.264    ± 153.306  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10635.266    ± 314.888  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10665.869    ± 116.540  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6509.603   ± 1790.880  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1073.984     ± 70.451  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6126.424   ± 1412.148  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1002.266     ± 75.526  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15      11848.150   ± 1788.176  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15187.912    ± 232.526  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4488.211     ± 60.093  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4550.412    ± 432.735  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6973.912    ± 119.514  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      36229.534    ± 441.905  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35729.374    ± 486.056  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     771930.400  ± 29811.674  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     741658.910  ± 37352.833  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     800813.049  ± 40474.635  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     801956.338  ± 23776.425  ops/s
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

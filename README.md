# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-01T04:14:59Z
- **Commit:** [`e574d32`](https://github.com/prometheus/client_java/commit/e574d3226951fbecda109554024dc1f99eb8d5d5)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 554.37M | ± 11187.41K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.40M | ± 371.91K | ops/s |
| prometheusLabelValuesInc | 114.06M | ± 2793.79K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.37M | ± 319.02K | ops/s |
| prometheusInc | 65.24K | ± 1.10K | ops/s |
| prometheusNoLabelsInc | 56.01K | ± 1.50K | ops/s |
| prometheusAdd | 50.72K | ± 1.01K | ops/s |
| codahaleIncNoLabels | 47.44K | ± 2.24K | ops/s |
| openTelemetryBoundInc | 37.56K | ± 595.64 | ops/s |
| openTelemetryBoundAdd | 31.68K | ± 820.29 | ops/s |
| openTelemetryInc | 23.13K | ± 455.29 | ops/s |
| openTelemetryIncNoLabels | 21.78K | ± 349.25 | ops/s |
| openTelemetryAdd | 19.25K | ± 319.99 | ops/s |
| simpleclientInc | 6.57K | ± 107.31 | ops/s |
| simpleclientNoLabelsInc | 6.38K | ± 193.61 | ops/s |
| simpleclientAdd | 6.22K | ± 180.53 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.01K | ± 121.56 | ops/s |
| prometheusClassic | 7.14K | ± 1.14K | ops/s |
| openTelemetryClassic | 4.84K | ± 746.48 | ops/s |
| openTelemetryBoundClassic | 4.70K | ± 2.48K | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 21.59 | ops/s |
| simpleclient | 4.41K | ± 89.62 | ops/s |
| prometheusNative | 2.61K | ± 362.03 | ops/s |
| openTelemetryBoundExponential | 1.09K | ± 46.67 | ops/s |
| openTelemetryExponential | 929.90 | ± 45.86 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.13K | ± 991.85 | ops/s |
| openMetricsWriteToNull | 30.54K | ± 956.17 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 507.01K | ± 2.34K | ops/s |
| openMetricsWriteToNull | 497.71K | ± 5.60K | ops/s |
| prometheusWriteToByteArray | 497.38K | ± 4.16K | ops/s |
| openMetricsWriteToByteArray | 481.74K | ± 4.46K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.247 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.861 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.198 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.008 | — | — |
| HistogramBenchmark.prometheusClassic | 0.528 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.671 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.643 | — | — |
| HistogramBenchmark.prometheusNative | 417713.451 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.116 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.113 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47436.020   ± 2238.093  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19250.137    ± 319.987  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31683.654    ± 820.285  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37560.611    ± 595.638  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23132.624    ± 455.286  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21778.777    ± 349.253  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50715.138   ± 1006.213  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  554370996.387 ± 11187412.103  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334403367.731 ± 371908.423  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65235.085   ± 1100.806  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114058832.776 ± 2793789.224  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58373670.087 ± 319016.728  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56007.339   ± 1504.227  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6219.903    ± 180.527  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6574.768    ± 107.309  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6376.821    ± 193.607  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4696.144   ± 2480.370  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1090.957     ± 46.673  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4838.326    ± 746.476  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        929.899     ± 45.856  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7137.881   ± 1142.120  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12010.028    ± 121.562  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4530.513     ± 21.588  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2613.682    ± 362.027  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4405.348     ± 89.622  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      30537.694    ± 956.173  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31133.621    ± 991.851  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481739.390   ± 4455.954  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     497707.152   ± 5595.642  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497384.175   ± 4157.327  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507009.624   ± 2339.145  ops/s
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

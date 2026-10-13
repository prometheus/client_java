# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-13T04:37:12Z
- **Commit:** [`8645917`](https://github.com/prometheus/client_java/commit/864591743fbba866152c4b5e57f54da970463ee8)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 552.16M | ± 13822.59K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.62M | ± 276.31K | ops/s |
| prometheusLabelValuesInc | 114.98M | ± 2185.02K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.73M | ± 331.05K | ops/s |
| prometheusInc | 63.97K | ± 1.55K | ops/s |
| prometheusNoLabelsInc | 55.96K | ± 1.01K | ops/s |
| prometheusAdd | 50.91K | ± 895.60 | ops/s |
| codahaleIncNoLabels | 47.64K | ± 743.77 | ops/s |
| openTelemetryBoundInc | 36.96K | ± 1.44K | ops/s |
| openTelemetryBoundAdd | 31.63K | ± 540.34 | ops/s |
| openTelemetryIncNoLabels | 23.26K | ± 1.27K | ops/s |
| openTelemetryInc | 17.61K | ± 724.41 | ops/s |
| openTelemetryAdd | 15.42K | ± 247.02 | ops/s |
| simpleclientInc | 6.52K | ± 87.71 | ops/s |
| simpleclientAdd | 6.37K | ± 75.09 | ops/s |
| simpleclientNoLabelsInc | 6.25K | ± 105.74 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.03K | ± 177.58 | ops/s |
| prometheusClassic | 5.65K | ± 1.00K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 15.00 | ops/s |
| openTelemetryClassic | 4.46K | ± 1.64K | ops/s |
| simpleclient | 4.38K | ± 72.56 | ops/s |
| openTelemetryBoundClassic | 4.35K | ± 538.69 | ops/s |
| prometheusNative | 2.75K | ± 296.30 | ops/s |
| openTelemetryBoundExponential | 1.03K | ± 79.40 | ops/s |
| openTelemetryExponential | 881.99 | ± 20.86 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.37K | ± 150.26 | ops/s |
| prometheusWriteToNull | 23.35K | ± 759.17 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 517.36K | ± 8.24K | ops/s |
| openMetricsWriteToByteArray | 501.20K | ± 2.58K | ops/s |
| prometheusWriteToByteArray | 499.91K | ± 3.23K | ops/s |
| openMetricsWriteToNull | 499.56K | ± 3.55K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.061 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.053 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.150 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.221 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.915 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.232 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.057 | — | — |
| HistogramBenchmark.prometheusClassic | 0.672 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.674 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.647 | — | — |
| HistogramBenchmark.prometheusNative | 417713.358 | — | — |
| HistogramBenchmark.simpleclient | 0.215 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.151 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.151 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47642.569    ± 743.769  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15420.221    ± 247.021  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31632.916    ± 540.345  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      36964.474   ± 1436.737  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17613.844    ± 724.413  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23260.684   ± 1269.933  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50914.649    ± 895.599  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  552160251.406 ± 13822589.722  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334617435.227 ± 276313.754  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63974.127   ± 1550.146  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114983034.885 ± 2185024.130  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58726952.344 ± 331051.873  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55957.743   ± 1006.045  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6369.831     ± 75.094  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6518.372     ± 87.711  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6253.580    ± 105.744  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4348.975    ± 538.693  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1027.043     ± 79.401  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4458.026   ± 1637.674  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        881.995     ± 20.863  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5647.732   ± 1003.142  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12033.676    ± 177.575  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4513.544     ± 15.005  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2750.579    ± 296.305  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4377.941     ± 72.555  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23367.648    ± 150.262  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23349.195    ± 759.168  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     501198.671   ± 2579.970  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     499555.630   ± 3550.704  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499909.088   ± 3233.750  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     517356.200   ± 8243.689  ops/s
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

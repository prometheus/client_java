# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-21T04:28:04Z
- **Commit:** [`c658f3d`](https://github.com/prometheus/client_java/commit/c658f3da2d8c6f5e90837dcd3681b744242d796d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 676.55M | ± 7116.69K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 371.07M | ± 167.80K | ops/s |
| prometheusLabelValuesInc | 138.64M | ± 2774.78K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.45M | ± 332.90K | ops/s |
| prometheusInc | 78.23K | ± 1.54K | ops/s |
| prometheusNoLabelsInc | 66.22K | ± 714.23 | ops/s |
| prometheusAdd | 62.98K | ± 806.30 | ops/s |
| codahaleIncNoLabels | 55.80K | ± 1.70K | ops/s |
| openTelemetryBoundInc | 45.53K | ± 124.29 | ops/s |
| openTelemetryBoundAdd | 36.67K | ± 3.10K | ops/s |
| openTelemetryIncNoLabels | 26.35K | ± 1.08K | ops/s |
| openTelemetryInc | 21.53K | ± 61.35 | ops/s |
| openTelemetryAdd | 18.98K | ± 301.44 | ops/s |
| simpleclientAdd | 7.95K | ± 91.11 | ops/s |
| simpleclientInc | 7.87K | ± 111.41 | ops/s |
| simpleclientNoLabelsInc | 7.60K | ± 17.85 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.53K | ± 66.85 | ops/s |
| openTelemetryBoundClassic | 7.91K | ± 1.02K | ops/s |
| prometheusClassic | 7.43K | ± 1.97K | ops/s |
| prometheusClassicSingleThread | 7.22K | ± 20.79 | ops/s |
| openTelemetryClassic | 6.06K | ± 1.07K | ops/s |
| simpleclient | 5.87K | ± 54.01 | ops/s |
| prometheusNative | 3.80K | ± 266.60 | ops/s |
| openTelemetryBoundExponential | 1.01K | ± 65.80 | ops/s |
| openTelemetryExponential | 851.35 | ± 50.50 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 35.55K | ± 237.02 | ops/s |
| prometheusWriteToNull | 34.77K | ± 1.16K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 806.86K | ± 4.31K | ops/s |
| prometheusWriteToByteArray | 785.00K | ± 12.76K | ops/s |
| openMetricsWriteToNull | 754.00K | ± 3.88K | ops/s |
| openMetricsWriteToByteArray | 736.66K | ± 5.96K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.017 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.026 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.020 | — | — |
| CounterBenchmark.openTelemetryInc | 0.043 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.035 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.047 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.117 | — | — |
| CounterBenchmark.simpleclientInc | 0.119 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.123 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.120 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.934 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.159 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.104 | — | — |
| HistogramBenchmark.prometheusClassic | 0.517 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.462 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.403 | — | — |
| HistogramBenchmark.prometheusNative | 335792.988 | — | — |
| HistogramBenchmark.simpleclient | 0.159 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.098 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.101 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      55797.117   ± 1701.090  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18978.075    ± 301.445  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      36671.529   ± 3100.226  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      45534.181    ± 124.289  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21530.498     ± 61.353  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      26346.155   ± 1076.988  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62984.721    ± 806.303  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  676552496.955 ± 7116685.821  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  371066523.100 ± 167796.770  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      78231.119   ± 1540.770  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  138635001.403 ± 2774780.055  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68450068.769 ± 332903.454  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66221.986    ± 714.230  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7954.635     ± 91.108  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7866.224    ± 111.412  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7604.679     ± 17.851  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       7914.365   ± 1017.098  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1005.470     ± 65.799  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6059.148   ± 1074.811  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        851.352     ± 50.503  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7430.427   ± 1968.222  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17533.545     ± 66.847  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7221.033     ± 20.788  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3797.594    ± 266.596  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5872.904     ± 54.015  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35548.626    ± 237.017  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34770.440   ± 1156.877  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     736656.751   ± 5962.031  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     753998.808   ± 3883.883  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     785001.907  ± 12759.188  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     806857.314   ± 4309.917  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-19T04:23:28Z
- **Commit:** [`c658f3d`](https://github.com/prometheus/client_java/commit/c658f3da2d8c6f5e90837dcd3681b744242d796d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 559.13M | ± 4267.65K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.96M | ± 209.92K | ops/s |
| prometheusLabelValuesInc | 112.69M | ± 6042.47K | ops/s |
| prometheusLabelValuesIncSingleThread | 56.83M | ± 2243.97K | ops/s |
| prometheusInc | 65.99K | ± 369.05 | ops/s |
| prometheusNoLabelsInc | 56.89K | ± 420.18 | ops/s |
| prometheusAdd | 51.19K | ± 569.51 | ops/s |
| codahaleIncNoLabels | 43.97K | ± 7.54K | ops/s |
| openTelemetryBoundInc | 37.87K | ± 211.57 | ops/s |
| openTelemetryBoundAdd | 31.85K | ± 97.24 | ops/s |
| openTelemetryIncNoLabels | 22.63K | ± 1.24K | ops/s |
| openTelemetryInc | 18.14K | ± 62.22 | ops/s |
| openTelemetryAdd | 15.62K | ± 75.37 | ops/s |
| simpleclientInc | 6.55K | ± 33.15 | ops/s |
| simpleclientAdd | 6.49K | ± 46.17 | ops/s |
| simpleclientNoLabelsInc | 6.47K | ± 115.49 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.06K | ± 25.30 | ops/s |
| prometheusClassic | 6.95K | ± 798.21 | ops/s |
| openTelemetryBoundClassic | 5.27K | ± 1.96K | ops/s |
| openTelemetryClassic | 4.59K | ± 838.45 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 10.53 | ops/s |
| simpleclient | 4.43K | ± 63.74 | ops/s |
| prometheusNative | 2.74K | ± 319.76 | ops/s |
| openTelemetryBoundExponential | 1.02K | ± 70.94 | ops/s |
| openTelemetryExponential | 798.49 | ± 90.44 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.48K | ± 601.75 | ops/s |
| openMetricsWriteToNull | 23.30K | ± 838.07 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 561.04K | ± 7.31K | ops/s |
| prometheusWriteToByteArray | 550.43K | ± 4.63K | ops/s |
| openMetricsWriteToByteArray | 515.75K | ± 6.59K | ops/s |
| openMetricsWriteToNull | 507.36K | ± 5.13K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.022 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.059 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.143 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.144 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.200 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.914 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.212 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.171 | — | — |
| HistogramBenchmark.prometheusClassic | 0.535 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.654 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.640 | — | — |
| HistogramBenchmark.prometheusNative | 417713.364 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.150 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.149 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43974.591   ± 7541.155  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15617.745     ± 75.366  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31848.110     ± 97.240  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37867.339    ± 211.574  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18143.030     ± 62.215  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22633.628   ± 1243.031  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51192.789    ± 569.507  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  559131329.202 ± 4267645.753  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334958173.746 ± 209920.043  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65992.397    ± 369.053  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  112691519.196 ± 6042472.928  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   56826269.173 ± 2243970.345  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56887.163    ± 420.180  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6493.170     ± 46.168  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6550.362     ± 33.151  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6470.542    ± 115.486  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5270.452   ± 1961.293  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1018.607     ± 70.937  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4588.644    ± 838.454  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        798.495     ± 90.440  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6947.232    ± 798.206  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12056.227     ± 25.304  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4542.945     ± 10.527  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2739.863    ± 319.761  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4425.755     ± 63.738  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23295.021    ± 838.069  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23476.758    ± 601.753  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     515753.806   ± 6590.915  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     507356.513   ± 5128.661  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     550430.913   ± 4626.059  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     561037.624   ± 7309.104  ops/s
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

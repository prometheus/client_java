# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-26T04:28:01Z
- **Commit:** [`533375f`](https://github.com/prometheus/client_java/commit/533375f65bc9130d9ea88ca139784bfb966b04f7)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 676.37M | ± 23737.35K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 211.63M | ± 3550.63K | ops/s |
| prometheusLabelValuesInc | 201.35M | ± 3149.37K | ops/s |
| prometheusLabelValuesIncSingleThread | 93.38M | ± 1180.66K | ops/s |
| prometheusInc | 66.98K | ± 1.18K | ops/s |
| prometheusNoLabelsInc | 65.25K | ± 469.86 | ops/s |
| codahaleIncNoLabels | 59.82K | ± 467.64 | ops/s |
| prometheusAdd | 55.57K | ± 523.18 | ops/s |
| openTelemetryBoundInc | 52.72K | ± 482.68 | ops/s |
| openTelemetryBoundAdd | 47.33K | ± 742.89 | ops/s |
| openTelemetryIncNoLabels | 34.76K | ± 435.10 | ops/s |
| openTelemetryInc | 33.53K | ± 1.91K | ops/s |
| openTelemetryAdd | 27.16K | ± 398.50 | ops/s |
| simpleclientNoLabelsInc | 10.71K | ± 159.77 | ops/s |
| simpleclientInc | 10.66K | ± 165.37 | ops/s |
| simpleclientAdd | 9.89K | ± 341.65 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 14.14K | ± 163.55 | ops/s |
| prometheusClassic | 8.24K | ± 2.05K | ops/s |
| simpleclient | 6.73K | ± 109.52 | ops/s |
| openTelemetryClassic | 6.45K | ± 2.97K | ops/s |
| prometheusNative | 4.68K | ± 246.81 | ops/s |
| prometheusClassicSingleThread | 4.14K | ± 58.59 | ops/s |
| openTelemetryBoundClassic | 3.86K | ± 115.34 | ops/s |
| openTelemetryBoundExponential | 1.09K | ± 54.24 | ops/s |
| openTelemetryExponential | 979.36 | ± 44.65 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 34.13K | ± 524.26 | ops/s |
| openMetricsWriteToNull | 33.90K | ± 504.18 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 809.09K | ± 51.94K | ops/s |
| prometheusWriteToByteArray | 788.53K | ± 22.86K | ops/s |
| openMetricsWriteToByteArray | 684.35K | ± 30.48K | ops/s |
| openMetricsWriteToNull | 683.78K | ± 43.93K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.035 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.018 | — | — |
| CounterBenchmark.openTelemetryInc | 0.028 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.027 | — | — |
| CounterBenchmark.prometheusAdd | 0.067 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.096 | — | — |
| CounterBenchmark.simpleclientInc | 0.089 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.088 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.243 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.871 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.196 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.967 | — | — |
| HistogramBenchmark.prometheusClassic | 0.478 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.563 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.667 | — | — |
| HistogramBenchmark.prometheusNative | 335792.812 | — | — |
| HistogramBenchmark.simpleclient | 0.141 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.104 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.103 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      59820.518    ± 467.645  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      27161.524    ± 398.503  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      47325.299    ± 742.893  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      52716.331    ± 482.682  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      33526.714   ± 1910.873  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      34756.643    ± 435.097  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      55567.893    ± 523.177  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  676370526.566 ± 23737346.042  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  211629618.120 ± 3550627.249  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66983.867   ± 1178.105  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  201353222.757 ± 3149369.303  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   93377149.447 ± 1180663.803  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      65252.745    ± 469.864  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       9889.825    ± 341.649  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10656.209    ± 165.372  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10705.482    ± 159.770  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3863.601    ± 115.339  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1091.270     ± 54.242  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6446.698   ± 2965.244  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        979.356     ± 44.646  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8236.441   ± 2047.101  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14137.469    ± 163.546  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4142.873     ± 58.589  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4682.479    ± 246.806  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6733.338    ± 109.516  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33904.629    ± 504.176  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34131.923    ± 524.256  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     684349.614  ± 30484.754  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     683783.717  ± 43933.979  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     788532.001  ± 22857.934  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     809087.365  ± 51939.175  ops/s
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

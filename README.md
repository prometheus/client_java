# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-12T04:14:08Z
- **Commit:** [`47bc9c7`](https://github.com/prometheus/client_java/commit/47bc9c7ae40c20715b8eb3bdc65d40f4c2327ded)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 652.74M | ± 32725.77K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 370.03M | ± 761.85K | ops/s |
| prometheusLabelValuesInc | 138.14M | ± 2578.85K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.06M | ± 417.45K | ops/s |
| prometheusInc | 75.77K | ± 2.06K | ops/s |
| prometheusNoLabelsInc | 65.34K | ± 482.74 | ops/s |
| prometheusAdd | 62.14K | ± 476.25 | ops/s |
| codahaleIncNoLabels | 57.07K | ± 561.31 | ops/s |
| openTelemetryBoundInc | 44.80K | ± 68.00 | ops/s |
| openTelemetryBoundAdd | 38.59K | ± 687.75 | ops/s |
| openTelemetryInc | 27.67K | ± 231.30 | ops/s |
| openTelemetryIncNoLabels | 27.60K | ± 548.74 | ops/s |
| openTelemetryAdd | 23.84K | ± 297.10 | ops/s |
| simpleclientInc | 7.78K | ± 131.96 | ops/s |
| simpleclientNoLabelsInc | 7.66K | ± 183.38 | ops/s |
| simpleclientAdd | 7.55K | ± 109.16 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.56K | ± 42.00 | ops/s |
| prometheusClassic | 9.07K | ± 1.42K | ops/s |
| prometheusClassicSingleThread | 6.32K | ± 1.60K | ops/s |
| simpleclient | 5.79K | ± 88.34 | ops/s |
| openTelemetryClassic | 5.71K | ± 363.40 | ops/s |
| openTelemetryBoundClassic | 4.75K | ± 832.45 | ops/s |
| prometheusNative | 3.41K | ± 277.18 | ops/s |
| openTelemetryBoundExponential | 973.99 | ± 72.54 | ops/s |
| openTelemetryExponential | 906.02 | ± 65.75 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 40.31K | ± 742.54 | ops/s |
| openMetricsWriteToNull | 40.26K | ± 381.39 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 764.84K | ± 22.67K | ops/s |
| prometheusWriteToByteArray | 764.12K | ± 19.47K | ops/s |
| openMetricsWriteToNull | 723.98K | ± 9.55K | ops/s |
| openMetricsWriteToByteArray | 702.65K | ± 8.05K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.039 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.034 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.049 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.057 | — | — |
| CounterBenchmark.simpleclientAdd | 0.125 | — | — |
| CounterBenchmark.simpleclientInc | 0.121 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.123 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.205 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.975 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.167 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.047 | — | — |
| HistogramBenchmark.prometheusClassic | 0.414 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.457 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.496 | — | — |
| HistogramBenchmark.prometheusNative | 417708.968 | — | — |
| HistogramBenchmark.simpleclient | 0.163 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.088 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.087 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57068.652    ± 561.309  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      23835.527    ± 297.099  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      38594.602    ± 687.755  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      44796.594     ± 68.000  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      27669.520    ± 231.298  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      27600.651    ± 548.737  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62143.345    ± 476.252  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  652742582.390 ± 32725767.428  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  370026125.672 ± 761852.404  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      75770.909   ± 2058.721  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  138139097.819 ± 2578851.346  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68061203.756 ± 417452.990  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      65336.928    ± 482.744  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7553.242    ± 109.165  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7782.456    ± 131.959  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7663.486    ± 183.379  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4754.566    ± 832.448  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        973.989     ± 72.545  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5712.332    ± 363.404  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        906.021     ± 65.748  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9074.602   ± 1422.709  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17560.956     ± 41.998  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       6321.833   ± 1597.326  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3413.002    ± 277.176  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5785.640     ± 88.338  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      40262.668    ± 381.386  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      40307.066    ± 742.542  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     702650.620   ± 8052.001  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     723975.360   ± 9551.540  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     764115.388  ± 19471.078  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     764839.510  ± 22669.967  ops/s
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

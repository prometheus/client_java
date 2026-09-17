# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-17T04:08:26Z
- **Commit:** [`f1bd53a`](https://github.com/prometheus/client_java/commit/f1bd53a3fe773529f8129e7088caf5051759be82)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 518.42M | ± 889.40K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 287.66M | ± 265.47K | ops/s |
| prometheusLabelValuesInc | 107.78M | ± 1733.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 53.18M | ± 127.71K | ops/s |
| prometheusInc | 58.35K | ± 2.53K | ops/s |
| prometheusNoLabelsInc | 51.24K | ± 476.50 | ops/s |
| prometheusAdd | 48.40K | ± 1.06K | ops/s |
| codahaleIncNoLabels | 44.18K | ± 981.97 | ops/s |
| openTelemetryIncNoLabels | 16.68K | ± 576.31 | ops/s |
| openTelemetryInc | 13.90K | ± 24.98 | ops/s |
| openTelemetryAdd | 12.21K | ± 31.56 | ops/s |
| simpleclientInc | 6.23K | ± 48.06 | ops/s |
| simpleclientAdd | 5.99K | ± 271.84 | ops/s |
| simpleclientNoLabelsInc | 5.92K | ± 27.82 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.40K | ± 89.28 | ops/s |
| prometheusClassic | 7.26K | ± 1.30K | ops/s |
| prometheusClassicSingleThread | 5.62K | ± 48.26 | ops/s |
| simpleclient | 4.43K | ± 55.97 | ops/s |
| prometheusNative | 2.73K | ± 149.08 | ops/s |
| openTelemetryClassic | 775.90 | ± 11.87 | ops/s |
| openTelemetryExponential | 616.11 | ± 53.79 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.57K | ± 124.10 | ops/s |
| openMetricsWriteToNull | 27.21K | ± 379.03 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 668.72K | ± 3.27K | ops/s |
| prometheusWriteToByteArray | 655.01K | ± 5.80K | ops/s |
| openMetricsWriteToNull | 619.53K | ± 5.04K | ops/s |
| openMetricsWriteToByteArray | 611.65K | ± 1.62K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.021 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.076 | — | — |
| CounterBenchmark.openTelemetryInc | 0.067 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.056 | — | — |
| CounterBenchmark.prometheusAdd | 0.076 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.063 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.072 | — | — |
| CounterBenchmark.simpleclientAdd | 0.156 | — | — |
| CounterBenchmark.simpleclientInc | 0.150 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.157 | — | — |
| HistogramBenchmark.openTelemetryClassic | 1.202 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.525 | — | — |
| HistogramBenchmark.prometheusClassic | 0.521 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.602 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.518 | — | — |
| HistogramBenchmark.prometheusNative | 417713.362 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.129 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.127 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44176.375    ± 981.972  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12208.155     ± 31.556  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13897.855     ± 24.977  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      16678.328    ± 576.310  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48401.005   ± 1055.715  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  518420446.535 ± 889398.297  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  287663174.893 ± 265466.438  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58351.983   ± 2525.185  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  107781339.985 ± 1733753.692  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   53183091.893 ± 127714.015  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51240.357    ± 476.499  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5987.372    ± 271.840  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6234.959     ± 48.057  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5919.267     ± 27.824  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        775.905     ± 11.866  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        616.105     ± 53.792  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7258.214   ± 1301.861  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13398.615     ± 89.285  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5619.895     ± 48.261  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2734.965    ± 149.083  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4427.138     ± 55.970  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27211.703    ± 379.028  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27571.862    ± 124.102  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     611654.927   ± 1619.398  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     619534.453   ± 5044.544  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     655009.156   ± 5803.548  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     668724.045   ± 3274.368  ops/s
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

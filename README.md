# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-07T04:13:48Z
- **Commit:** [`85d9fb0`](https://github.com/prometheus/client_java/commit/85d9fb0dca9f064c746470e91b924d253abf8d30)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 249.75M | ± 8477.65K | ops/s |
| prometheusLabelValuesInc | 122.15M | ± 649.94K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 92.24M | ± 134.97K | ops/s |
| prometheusLabelValuesIncSingleThread | 59.56M | ± 570.32K | ops/s |
| codahaleIncNoLabels | 26.48K | ± 150.92 | ops/s |
| prometheusInc | 25.67K | ± 44.55 | ops/s |
| prometheusNoLabelsInc | 25.66K | ± 404.37 | ops/s |
| openTelemetryBoundInc | 25.31K | ± 141.04 | ops/s |
| prometheusAdd | 24.64K | ± 754.03 | ops/s |
| openTelemetryBoundAdd | 22.92K | ± 139.55 | ops/s |
| openTelemetryIncNoLabels | 19.97K | ± 1.44K | ops/s |
| openTelemetryInc | 19.86K | ± 647.81 | ops/s |
| openTelemetryAdd | 17.00K | ± 592.38 | ops/s |
| simpleclientInc | 6.55K | ± 34.99 | ops/s |
| simpleclientNoLabelsInc | 6.37K | ± 87.36 | ops/s |
| simpleclientAdd | 6.14K | ± 185.58 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.59K | ± 33.98 | ops/s |
| simpleclient | 4.06K | ± 62.89 | ops/s |
| prometheusClassicSingleThread | 2.85K | ± 61.17 | ops/s |
| openTelemetryClassic | 2.82K | ± 1.45K | ops/s |
| prometheusClassic | 2.80K | ± 702.16 | ops/s |
| openTelemetryBoundClassic | 2.68K | ± 570.32 | ops/s |
| prometheusNative | 2.00K | ± 137.79 | ops/s |
| openTelemetryBoundExponential | 712.31 | ± 44.41 | ops/s |
| openTelemetryExponential | 550.62 | ± 57.16 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.65K | ± 50.08 | ops/s |
| openMetricsWriteToNull | 18.60K | ± 76.52 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 307.25K | ± 4.28K | ops/s |
| prometheusWriteToByteArray | 304.90K | ± 1.93K | ops/s |
| openMetricsWriteToNull | 283.43K | ± 2.84K | ops/s |
| openMetricsWriteToByteArray | 280.81K | ± 3.50K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.035 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.055 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.041 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.037 | — | — |
| CounterBenchmark.openTelemetryInc | 0.047 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.047 | — | — |
| CounterBenchmark.prometheusAdd | 0.150 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.144 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.144 | — | — |
| CounterBenchmark.simpleclientAdd | 0.153 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.367 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.323 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.381 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.715 | — | — |
| HistogramBenchmark.prometheusClassic | 1.402 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.232 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.980 | — | — |
| HistogramBenchmark.prometheusNative | 417713.881 | — | — |
| HistogramBenchmark.simpleclient | 0.231 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.189 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.189 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.003 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.336 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      26475.733    ± 150.922  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      16996.876    ± 592.384  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      22921.603    ± 139.549  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      25307.458    ± 141.042  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      19857.885    ± 647.807  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      19965.146   ± 1441.093  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      24642.173    ± 754.028  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  249746352.540 ± 8477649.066  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   92241025.353 ± 134965.751  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      25668.147     ± 44.552  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  122150566.358 ± 649939.697  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   59564184.412 ± 570317.078  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      25659.721    ± 404.375  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6140.271    ± 185.579  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6551.578     ± 34.992  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6365.294     ± 87.363  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2682.000    ± 570.324  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        712.312     ± 44.415  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2819.400   ± 1454.418  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        550.623     ± 57.158  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2797.712    ± 702.163  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6586.014     ± 33.984  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2849.059     ± 61.167  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1995.044    ± 137.788  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4061.615     ± 62.885  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18602.444     ± 76.524  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18652.085     ± 50.083  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     280808.959   ± 3504.288  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     283431.845   ± 2838.127  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     304904.662   ± 1927.190  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     307246.817   ± 4283.934  ops/s
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

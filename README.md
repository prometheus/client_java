# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-20T04:24:40Z
- **Commit:** [`c658f3d`](https://github.com/prometheus/client_java/commit/c658f3da2d8c6f5e90837dcd3681b744242d796d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 680.99M | ± 12565.05K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 370.75M | ± 465.00K | ops/s |
| prometheusLabelValuesInc | 141.41M | ± 488.25K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.58M | ± 800.24K | ops/s |
| prometheusInc | 72.73K | ± 2.61K | ops/s |
| prometheusNoLabelsInc | 65.40K | ± 478.07 | ops/s |
| prometheusAdd | 62.63K | ± 1.55K | ops/s |
| codahaleIncNoLabels | 57.00K | ± 788.57 | ops/s |
| openTelemetryBoundInc | 45.33K | ± 511.65 | ops/s |
| openTelemetryBoundAdd | 39.46K | ± 112.59 | ops/s |
| openTelemetryIncNoLabels | 26.15K | ± 529.19 | ops/s |
| openTelemetryInc | 21.49K | ± 102.65 | ops/s |
| openTelemetryAdd | 19.11K | ± 134.78 | ops/s |
| simpleclientInc | 8.00K | ± 11.90 | ops/s |
| simpleclientAdd | 7.84K | ± 244.41 | ops/s |
| simpleclientNoLabelsInc | 7.59K | ± 21.40 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.21K | ± 83.89 | ops/s |
| prometheusClassic | 9.49K | ± 199.87 | ops/s |
| openTelemetryBoundClassic | 9.40K | ± 1.57K | ops/s |
| prometheusClassicSingleThread | 7.21K | ± 23.59 | ops/s |
| simpleclient | 5.74K | ± 142.43 | ops/s |
| openTelemetryClassic | 4.98K | ± 470.83 | ops/s |
| prometheusNative | 3.81K | ± 200.93 | ops/s |
| openTelemetryBoundExponential | 897.51 | ± 58.31 | ops/s |
| openTelemetryExponential | 824.02 | ± 11.19 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 35.31K | ± 346.15 | ops/s |
| openMetricsWriteToNull | 35.31K | ± 98.08 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 800.84K | ± 8.86K | ops/s |
| prometheusWriteToByteArray | 780.04K | ± 5.11K | ops/s |
| openMetricsWriteToNull | 742.58K | ± 4.19K | ops/s |
| openMetricsWriteToByteArray | 728.23K | ± 5.54K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.043 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.036 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.051 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.119 | — | — |
| CounterBenchmark.simpleclientInc | 0.117 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.123 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.103 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.046 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.191 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.138 | — | — |
| HistogramBenchmark.prometheusClassic | 0.390 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.471 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.404 | — | — |
| HistogramBenchmark.prometheusNative | 417712.973 | — | — |
| HistogramBenchmark.simpleclient | 0.163 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.099 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.099 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      56995.598    ± 788.567  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19107.560    ± 134.778  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      39458.328    ± 112.592  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      45330.570    ± 511.646  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21494.985    ± 102.653  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      26149.917    ± 529.191  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62633.272   ± 1546.321  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  680991395.916 ± 12565048.326  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  370753396.474 ± 465002.813  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      72734.970   ± 2606.038  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  141413854.850 ± 488248.452  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68579735.192 ± 800244.023  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      65402.677    ± 478.070  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7836.278    ± 244.410  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7997.369     ± 11.902  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7589.152     ± 21.398  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       9398.365   ± 1570.375  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        897.512     ± 58.314  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4975.976    ± 470.832  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        824.019     ± 11.186  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9485.350    ± 199.874  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17208.627     ± 83.891  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7212.363     ± 23.586  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3811.237    ± 200.929  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5743.021    ± 142.433  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35309.191     ± 98.079  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35309.343    ± 346.154  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     728226.348   ± 5538.014  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     742579.295   ± 4191.916  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     780042.765   ± 5106.592  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     800844.696   ± 8859.395  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-27T04:24:31Z
- **Commit:** [`7695470`](https://github.com/prometheus/client_java/commit/76954702079ded14718b84cbdde7f3bcf1bc03bd)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 660.82M | ± 33628.56K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 211.96M | ± 2854.68K | ops/s |
| prometheusLabelValuesInc | 196.39M | ± 4181.47K | ops/s |
| prometheusLabelValuesIncSingleThread | 93.97M | ± 760.32K | ops/s |
| prometheusInc | 65.57K | ± 683.85 | ops/s |
| prometheusNoLabelsInc | 63.91K | ± 2.49K | ops/s |
| codahaleIncNoLabels | 61.52K | ± 1.02K | ops/s |
| prometheusAdd | 58.40K | ± 2.00K | ops/s |
| openTelemetryBoundInc | 53.37K | ± 1.00K | ops/s |
| openTelemetryBoundAdd | 47.50K | ± 881.72 | ops/s |
| openTelemetryInc | 34.83K | ± 538.17 | ops/s |
| openTelemetryIncNoLabels | 34.69K | ± 519.92 | ops/s |
| openTelemetryAdd | 29.76K | ± 1.65K | ops/s |
| simpleclientInc | 10.76K | ± 120.94 | ops/s |
| simpleclientNoLabelsInc | 10.65K | ± 234.18 | ops/s |
| simpleclientAdd | 10.11K | ± 181.54 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 14.79K | ± 59.15 | ops/s |
| prometheusClassic | 8.58K | ± 2.53K | ops/s |
| simpleclient | 6.61K | ± 121.45 | ops/s |
| openTelemetryClassic | 6.51K | ± 1.73K | ops/s |
| openTelemetryBoundClassic | 5.96K | ± 1.88K | ops/s |
| prometheusNative | 4.75K | ± 443.84 | ops/s |
| prometheusClassicSingleThread | 4.31K | ± 30.39 | ops/s |
| openTelemetryBoundExponential | 1.15K | ± 60.72 | ops/s |
| openTelemetryExponential | 1.10K | ± 48.37 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 33.64K | ± 631.89 | ops/s |
| prometheusWriteToNull | 32.87K | ± 981.50 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 790.32K | ± 35.35K | ops/s |
| prometheusWriteToByteArray | 781.35K | ± 38.37K | ops/s |
| openMetricsWriteToNull | 713.35K | ± 11.05K | ops/s |
| openMetricsWriteToByteArray | 701.91K | ± 38.72K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.032 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.018 | — | — |
| CounterBenchmark.openTelemetryInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.027 | — | — |
| CounterBenchmark.prometheusAdd | 0.063 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.058 | — | — |
| CounterBenchmark.simpleclientAdd | 0.093 | — | — |
| CounterBenchmark.simpleclientInc | 0.088 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.088 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.171 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.831 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.153 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.861 | — | — |
| HistogramBenchmark.prometheusClassic | 0.464 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.542 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.656 | — | — |
| HistogramBenchmark.prometheusNative | 253872.808 | — | — |
| HistogramBenchmark.simpleclient | 0.143 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.105 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.107 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18477.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      61515.022   ± 1018.792  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      29764.814   ± 1652.800  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      47504.877    ± 881.720  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      53374.796   ± 1003.417  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      34828.168    ± 538.169  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      34689.919    ± 519.923  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      58404.822   ± 1996.370  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  660824285.037 ± 33628557.867  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  211958613.043 ± 2854679.466  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65572.893    ± 683.847  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  196390599.236 ± 4181474.448  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   93972697.479 ± 760320.213  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      63910.584   ± 2491.049  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10110.492    ± 181.539  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10760.411    ± 120.942  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10646.410    ± 234.176  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5961.411   ± 1875.669  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1147.184     ± 60.719  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6508.801   ± 1727.395  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1098.693     ± 48.368  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8581.626   ± 2534.956  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14787.991     ± 59.148  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4305.550     ± 30.392  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4746.090    ± 443.836  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6612.425    ± 121.452  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33641.823    ± 631.887  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      32873.933    ± 981.502  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     701913.920  ± 38717.675  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     713347.437  ± 11049.480  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     781354.021  ± 38374.413  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     790318.619  ± 35346.594  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-05T04:15:20Z
- **Commit:** [`5c3d2c0`](https://github.com/prometheus/client_java/commit/5c3d2c01497e0f6a5ae7709d023255529468171c)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 553.79M | ± 13043.58K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 335.08M | ± 478.65K | ops/s |
| prometheusLabelValuesInc | 116.87M | ± 3347.04K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.65M | ± 511.64K | ops/s |
| prometheusInc | 65.39K | ± 1.12K | ops/s |
| prometheusNoLabelsInc | 56.14K | ± 934.51 | ops/s |
| prometheusAdd | 50.83K | ± 911.95 | ops/s |
| codahaleIncNoLabels | 47.26K | ± 2.20K | ops/s |
| openTelemetryBoundInc | 36.23K | ± 2.34K | ops/s |
| openTelemetryBoundAdd | 31.76K | ± 702.56 | ops/s |
| openTelemetryInc | 23.32K | ± 304.35 | ops/s |
| openTelemetryIncNoLabels | 21.56K | ± 531.67 | ops/s |
| openTelemetryAdd | 19.38K | ± 296.16 | ops/s |
| simpleclientInc | 6.62K | ± 93.72 | ops/s |
| simpleclientNoLabelsInc | 6.46K | ± 140.05 | ops/s |
| simpleclientAdd | 6.34K | ± 129.06 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.05K | ± 158.81 | ops/s |
| openTelemetryBoundClassic | 4.74K | ± 1.26K | ops/s |
| openTelemetryClassic | 4.56K | ± 974.58 | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 28.70 | ops/s |
| prometheusClassic | 4.50K | ± 568.63 | ops/s |
| simpleclient | 4.43K | ± 74.20 | ops/s |
| prometheusNative | 2.61K | ± 355.46 | ops/s |
| openTelemetryBoundExponential | 1.02K | ± 49.12 | ops/s |
| openTelemetryExponential | 923.25 | ± 85.64 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 30.00K | ± 1.08K | ops/s |
| prometheusWriteToNull | 29.90K | ± 966.47 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 513.61K | ± 3.42K | ops/s |
| prometheusWriteToByteArray | 501.72K | ± 6.79K | ops/s |
| openMetricsWriteToNull | 500.61K | ± 6.67K | ops/s |
| openMetricsWriteToByteArray | 492.83K | ± 4.97K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.026 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.145 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.213 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.923 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.214 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.024 | — | — |
| HistogramBenchmark.prometheusClassic | 0.831 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.665 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.644 | — | — |
| HistogramBenchmark.prometheusNative | 417713.445 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.118 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.118 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47256.431   ± 2195.650  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19383.944    ± 296.161  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31760.663    ± 702.562  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      36226.109   ± 2341.779  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23315.169    ± 304.352  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21561.603    ± 531.665  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50828.206    ± 911.949  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  553787944.891 ± 13043581.574  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  335078794.009 ± 478648.425  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65394.506   ± 1115.306  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116874094.173 ± 3347043.195  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58645653.992 ± 511638.764  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56139.782    ± 934.513  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6343.119    ± 129.062  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6622.341     ± 93.718  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6460.451    ± 140.053  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4741.610   ± 1261.708  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1019.812     ± 49.118  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4563.860    ± 974.577  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        923.248     ± 85.640  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4501.389    ± 568.627  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12045.567    ± 158.814  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4531.342     ± 28.700  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2609.777    ± 355.462  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4426.560     ± 74.199  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29997.434   ± 1079.266  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      29896.655    ± 966.465  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     492827.433   ± 4970.755  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     500607.693   ± 6666.297  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     501719.408   ± 6792.636  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513605.981   ± 3418.978  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-20T04:14:29Z
- **Commit:** [`a6614b6`](https://github.com/prometheus/client_java/commit/a6614b6778d9e27b4e8d8c8e5fc8ce41f6575c3f)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 553.83M | ± 10958.83K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.88M | ± 249.24K | ops/s |
| prometheusLabelValuesInc | 110.49M | ± 10537.90K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.45M | ± 179.34K | ops/s |
| prometheusInc | 62.73K | ± 4.27K | ops/s |
| prometheusNoLabelsInc | 56.42K | ± 986.81 | ops/s |
| prometheusAdd | 50.50K | ± 640.83 | ops/s |
| codahaleIncNoLabels | 49.03K | ± 1.69K | ops/s |
| openTelemetryBoundInc | 37.85K | ± 624.58 | ops/s |
| openTelemetryBoundAdd | 32.10K | ± 464.35 | ops/s |
| openTelemetryIncNoLabels | 23.29K | ± 1.31K | ops/s |
| openTelemetryInc | 23.15K | ± 346.15 | ops/s |
| openTelemetryAdd | 19.35K | ± 262.94 | ops/s |
| simpleclientInc | 6.60K | ± 121.16 | ops/s |
| simpleclientNoLabelsInc | 6.44K | ± 169.02 | ops/s |
| simpleclientAdd | 6.32K | ± 188.96 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.02K | ± 168.71 | ops/s |
| prometheusClassic | 5.55K | ± 1.60K | ops/s |
| openTelemetryClassic | 4.81K | ± 928.91 | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 17.68 | ops/s |
| simpleclient | 4.47K | ± 88.67 | ops/s |
| openTelemetryBoundClassic | 4.08K | ± 250.06 | ops/s |
| prometheusNative | 2.53K | ± 75.16 | ops/s |
| openTelemetryBoundExponential | 950.48 | ± 92.30 | ops/s |
| openTelemetryExponential | 919.37 | ± 74.36 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.55K | ± 553.79 | ops/s |
| prometheusWriteToNull | 31.38K | ± 664.05 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 514.38K | ± 5.35K | ops/s |
| prometheusWriteToByteArray | 504.45K | ± 5.82K | ops/s |
| openMetricsWriteToNull | 501.80K | ± 2.76K | ops/s |
| openMetricsWriteToByteArray | 496.12K | ± 6.19K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.059 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.145 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.233 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.994 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.203 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.030 | — | — |
| HistogramBenchmark.prometheusClassic | 0.714 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.658 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.461 | — | — |
| HistogramBenchmark.simpleclient | 0.210 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.112 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.112 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49028.880   ± 1685.078  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19346.112    ± 262.945  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      32101.208    ± 464.350  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37854.931    ± 624.580  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23154.444    ± 346.148  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23286.921   ± 1311.162  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50501.237    ± 640.835  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  553832296.042 ± 10958827.814  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334878365.095 ± 249235.741  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62730.937   ± 4265.732  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  110491086.497 ± 10537900.907  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58454517.821 ± 179342.201  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56422.784    ± 986.811  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6321.446    ± 188.959  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6602.233    ± 121.158  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6435.073    ± 169.020  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4078.587    ± 250.059  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        950.478     ± 92.299  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4810.631    ± 928.912  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        919.367     ± 74.356  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5554.731   ± 1601.775  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12015.571    ± 168.706  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4516.999     ± 17.678  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2527.204     ± 75.160  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4468.051     ± 88.669  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31551.957    ± 553.790  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31384.650    ± 664.046  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     496115.707   ± 6186.018  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     501798.037   ± 2758.590  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     504445.995   ± 5818.833  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     514382.777   ± 5352.618  ops/s
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

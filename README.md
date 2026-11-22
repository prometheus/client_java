# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-22T04:15:36Z
- **Commit:** [`976ef13`](https://github.com/prometheus/client_java/commit/976ef13e6831424335b6f58f7301152d745a5346)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 555.60M | ± 13147.92K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.91M | ± 429.38K | ops/s |
| prometheusLabelValuesInc | 117.05M | ± 2840.82K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.87M | ± 387.03K | ops/s |
| prometheusInc | 66.18K | ± 1.05K | ops/s |
| prometheusNoLabelsInc | 54.93K | ± 2.51K | ops/s |
| prometheusAdd | 51.19K | ± 864.02 | ops/s |
| codahaleIncNoLabels | 48.52K | ± 1.67K | ops/s |
| openTelemetryBoundInc | 37.82K | ± 650.61 | ops/s |
| openTelemetryBoundAdd | 32.00K | ± 685.17 | ops/s |
| openTelemetryIncNoLabels | 24.08K | ± 397.05 | ops/s |
| openTelemetryInc | 23.32K | ± 379.52 | ops/s |
| openTelemetryAdd | 19.47K | ± 284.14 | ops/s |
| simpleclientInc | 6.54K | ± 123.47 | ops/s |
| simpleclientNoLabelsInc | 6.40K | ± 219.15 | ops/s |
| simpleclientAdd | 5.92K | ± 247.41 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 171.26 | ops/s |
| prometheusClassic | 6.67K | ± 64.72 | ops/s |
| openTelemetryClassic | 4.66K | ± 1.08K | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 17.77 | ops/s |
| simpleclient | 4.46K | ± 91.78 | ops/s |
| prometheusNative | 2.68K | ± 341.42 | ops/s |
| openTelemetryBoundClassic | 2.58K | ± 456.79 | ops/s |
| openTelemetryBoundExponential | 1.01K | ± 103.23 | ops/s |
| openTelemetryExponential | 947.99 | ± 14.04 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.02K | ± 1.10K | ops/s |
| prometheusWriteToNull | 30.07K | ± 235.01 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 525.28K | ± 4.10K | ops/s |
| prometheusWriteToByteArray | 511.23K | ± 4.36K | ops/s |
| openMetricsWriteToNull | 498.31K | ± 3.75K | ops/s |
| openMetricsWriteToByteArray | 492.08K | ± 10.32K | ops/s |

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
| CounterBenchmark.openTelemetryIncNoLabels | 0.039 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.067 | — | — |
| CounterBenchmark.simpleclientAdd | 0.159 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.373 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.934 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.212 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.988 | — | — |
| HistogramBenchmark.prometheusClassic | 0.554 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.681 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.644 | — | — |
| HistogramBenchmark.prometheusNative | 335793.400 | — | — |
| HistogramBenchmark.simpleclient | 0.211 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.114 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.117 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48518.729   ± 1670.463  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19471.229    ± 284.139  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31997.960    ± 685.167  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37823.214    ± 650.605  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23322.502    ± 379.523  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      24079.091    ± 397.048  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51185.064    ± 864.022  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  555601633.295 ± 13147921.686  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334913659.994 ± 429379.425  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66177.377   ± 1051.230  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  117050352.864 ± 2840816.334  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58874921.163 ± 387025.511  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54933.264   ± 2508.355  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5922.268    ± 247.410  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6544.230    ± 123.473  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6399.439    ± 219.148  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2582.147    ± 456.790  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1011.955    ± 103.231  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4660.480   ± 1084.877  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        947.995     ± 14.041  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6666.846     ± 64.725  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12003.346    ± 171.265  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4528.789     ± 17.770  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2679.016    ± 341.421  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4456.416     ± 91.783  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31021.161   ± 1101.320  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30065.724    ± 235.009  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     492081.115  ± 10321.444  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     498306.064   ± 3748.443  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     511234.709   ± 4363.040  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     525283.365   ± 4095.566  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-24T04:15:55Z
- **Commit:** [`976ef13`](https://github.com/prometheus/client_java/commit/976ef13e6831424335b6f58f7301152d745a5346)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 556.56M | ± 11805.73K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.91M | ± 347.35K | ops/s |
| prometheusLabelValuesInc | 114.01M | ± 2509.92K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.39M | ± 321.02K | ops/s |
| prometheusInc | 63.72K | ± 1.82K | ops/s |
| prometheusNoLabelsInc | 55.59K | ± 1.19K | ops/s |
| prometheusAdd | 50.78K | ± 1.24K | ops/s |
| codahaleIncNoLabels | 49.83K | ± 853.63 | ops/s |
| openTelemetryBoundInc | 37.77K | ± 498.45 | ops/s |
| openTelemetryBoundAdd | 31.60K | ± 592.54 | ops/s |
| openTelemetryInc | 22.91K | ± 434.78 | ops/s |
| openTelemetryIncNoLabels | 22.27K | ± 1.06K | ops/s |
| openTelemetryAdd | 19.30K | ± 315.46 | ops/s |
| simpleclientInc | 6.49K | ± 116.76 | ops/s |
| simpleclientNoLabelsInc | 6.29K | ± 112.96 | ops/s |
| simpleclientAdd | 6.20K | ± 331.85 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.04K | ± 209.00 | ops/s |
| prometheusClassic | 7.12K | ± 1.45K | ops/s |
| openTelemetryClassic | 5.33K | ± 990.39 | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 13.46 | ops/s |
| simpleclient | 4.45K | ± 50.98 | ops/s |
| openTelemetryBoundClassic | 3.17K | ± 843.31 | ops/s |
| prometheusNative | 2.90K | ± 218.62 | ops/s |
| openTelemetryBoundExponential | 1.06K | ± 121.50 | ops/s |
| openTelemetryExponential | 975.44 | ± 37.02 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.04K | ± 403.29 | ops/s |
| openMetricsWriteToNull | 29.58K | ± 823.62 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 518.33K | ± 3.29K | ops/s |
| prometheusWriteToByteArray | 513.93K | ± 3.33K | ops/s |
| openMetricsWriteToNull | 506.94K | ± 3.17K | ops/s |
| openMetricsWriteToByteArray | 496.23K | ± 6.16K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.319 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.897 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.183 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.964 | — | — |
| HistogramBenchmark.prometheusClassic | 0.534 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.675 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.646 | — | — |
| HistogramBenchmark.prometheusNative | 417713.279 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.119 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.114 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18442.668 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49834.867    ± 853.629  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19302.359    ± 315.456  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31600.518    ± 592.540  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37766.525    ± 498.453  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22912.171    ± 434.776  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22271.450   ± 1061.765  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50778.404   ± 1238.093  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  556559039.688 ± 11805734.906  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334906646.554 ± 347349.575  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63716.999   ± 1816.245  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114014336.786 ± 2509917.577  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58394140.513 ± 321019.738  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55587.315   ± 1188.472  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6203.296    ± 331.855  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6489.155    ± 116.755  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6288.316    ± 112.959  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3165.587    ± 843.313  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1058.877    ± 121.501  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5326.314    ± 990.394  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        975.436     ± 37.019  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7116.708   ± 1448.357  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12036.019    ± 209.001  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4514.805     ± 13.460  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2902.488    ± 218.618  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4445.537     ± 50.979  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29580.625    ± 823.623  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31044.941    ± 403.285  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     496234.537   ± 6156.266  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     506935.457   ± 3168.825  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     513928.024   ± 3329.801  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     518329.061   ± 3289.667  ops/s
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

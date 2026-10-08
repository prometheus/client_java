# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-08T04:31:15Z
- **Commit:** [`d8089b2`](https://github.com/prometheus/client_java/commit/d8089b219004512f136a5e3d6153df74ddfbe748)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 522.73M | ± 35369.70K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.51M | ± 631.54K | ops/s |
| prometheusLabelValuesInc | 115.95M | ± 2005.42K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.28M | ± 397.77K | ops/s |
| prometheusInc | 64.02K | ± 2.02K | ops/s |
| prometheusNoLabelsInc | 55.47K | ± 2.11K | ops/s |
| prometheusAdd | 51.27K | ± 717.68 | ops/s |
| codahaleIncNoLabels | 49.37K | ± 1.12K | ops/s |
| openTelemetryBoundInc | 36.16K | ± 2.80K | ops/s |
| openTelemetryBoundAdd | 31.31K | ± 556.99 | ops/s |
| openTelemetryIncNoLabels | 21.75K | ± 330.34 | ops/s |
| openTelemetryInc | 18.05K | ± 289.19 | ops/s |
| openTelemetryAdd | 15.47K | ± 240.73 | ops/s |
| simpleclientInc | 6.59K | ± 118.50 | ops/s |
| simpleclientNoLabelsInc | 6.22K | ± 131.33 | ops/s |
| simpleclientAdd | 5.90K | ± 142.26 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.96K | ± 166.24 | ops/s |
| prometheusClassic | 6.25K | ± 1.32K | ops/s |
| openTelemetryBoundClassic | 5.99K | ± 1.44K | ops/s |
| prometheusClassicSingleThread | 4.49K | ± 88.09 | ops/s |
| simpleclient | 4.41K | ± 73.44 | ops/s |
| openTelemetryClassic | 3.70K | ± 205.83 | ops/s |
| prometheusNative | 2.88K | ± 272.54 | ops/s |
| openTelemetryBoundExponential | 1.04K | ± 111.45 | ops/s |
| openTelemetryExponential | 872.34 | ± 44.01 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.74K | ± 308.92 | ops/s |
| prometheusWriteToNull | 22.83K | ± 126.24 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 502.24K | ± 3.46K | ops/s |
| prometheusWriteToByteArray | 491.58K | ± 5.95K | ops/s |
| openMetricsWriteToNull | 481.64K | ± 10.94K | ops/s |
| openMetricsWriteToByteArray | 475.97K | ± 5.90K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.026 | — | — |
| CounterBenchmark.openTelemetryInc | 0.052 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.158 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.150 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.163 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.909 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.252 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.067 | — | — |
| HistogramBenchmark.prometheusClassic | 0.617 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.682 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.649 | — | — |
| HistogramBenchmark.prometheusNative | 417600.610 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.147 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.153 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49371.404   ± 1123.931  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15469.240    ± 240.727  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31313.182    ± 556.988  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      36161.666   ± 2802.085  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18045.817    ± 289.187  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21753.968    ± 330.339  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51266.968    ± 717.682  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  522726524.988 ± 35369704.404  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334506354.474 ± 631541.161  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64022.022   ± 2024.937  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115949352.255 ± 2005419.794  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58284524.186 ± 397774.304  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55467.552   ± 2114.771  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5898.172    ± 142.259  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6592.484    ± 118.501  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6221.016    ± 131.326  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5991.154   ± 1444.811  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1036.804    ± 111.446  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3696.518    ± 205.833  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        872.338     ± 44.011  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6249.638   ± 1319.591  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11956.278    ± 166.244  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4486.789     ± 88.095  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2882.876    ± 272.544  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4410.138     ± 73.436  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23741.193    ± 308.923  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22834.288    ± 126.239  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475969.523   ± 5897.796  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481635.814  ± 10940.997  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491579.917   ± 5951.277  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502244.972   ± 3461.259  ops/s
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

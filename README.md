# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-02T04:27:12Z
- **Commit:** [`9e9deb6`](https://github.com/prometheus/client_java/commit/9e9deb6b9e591c2a62b12882d8a987f120661b71)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 551.71M | ± 9314.83K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.10M | ± 1288.54K | ops/s |
| prometheusLabelValuesInc | 116.62M | ± 785.83K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.78M | ± 735.42K | ops/s |
| prometheusInc | 66.10K | ± 372.29 | ops/s |
| prometheusNoLabelsInc | 56.62K | ± 376.55 | ops/s |
| prometheusAdd | 51.16K | ± 259.68 | ops/s |
| codahaleIncNoLabels | 49.07K | ± 1.52K | ops/s |
| openTelemetryBoundInc | 37.82K | ± 234.06 | ops/s |
| openTelemetryBoundAdd | 31.81K | ± 313.38 | ops/s |
| openTelemetryIncNoLabels | 21.93K | ± 164.13 | ops/s |
| openTelemetryInc | 17.83K | ± 257.75 | ops/s |
| openTelemetryAdd | 15.56K | ± 28.09 | ops/s |
| simpleclientInc | 6.43K | ± 227.74 | ops/s |
| simpleclientNoLabelsInc | 6.36K | ± 29.06 | ops/s |
| simpleclientAdd | 6.33K | ± 210.35 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.06K | ± 26.01 | ops/s |
| prometheusClassic | 6.94K | ± 1.33K | ops/s |
| openTelemetryBoundClassic | 6.56K | ± 1.45K | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 13.57 | ops/s |
| simpleclient | 4.44K | ± 88.73 | ops/s |
| openTelemetryClassic | 3.82K | ± 111.98 | ops/s |
| prometheusNative | 3.11K | ± 39.95 | ops/s |
| openTelemetryBoundExponential | 1.06K | ± 92.22 | ops/s |
| openTelemetryExponential | 832.01 | ± 34.16 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.58K | ± 295.88 | ops/s |
| openMetricsWriteToNull | 23.39K | ± 385.54 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 532.44K | ± 9.17K | ops/s |
| prometheusWriteToByteArray | 519.41K | ± 12.08K | ops/s |
| openMetricsWriteToNull | 502.57K | ± 5.04K | ops/s |
| openMetricsWriteToByteArray | 489.86K | ± 9.37K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.052 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.145 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.881 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.245 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.118 | — | — |
| HistogramBenchmark.prometheusClassic | 0.546 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.667 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.641 | — | — |
| HistogramBenchmark.prometheusNative | 335793.202 | — | — |
| HistogramBenchmark.simpleclient | 0.210 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.150 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.148 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49071.323   ± 1523.024  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15558.659     ± 28.095  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31809.514    ± 313.380  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37823.528    ± 234.064  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17834.871    ± 257.747  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21926.377    ± 164.127  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51155.555    ± 259.679  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  551705684.067 ± 9314831.721  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334104116.621 ± 1288536.164  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66104.224    ± 372.294  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116616429.013 ± 785831.732  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58778287.759 ± 735422.098  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56624.956    ± 376.550  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6330.480    ± 210.355  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6433.354    ± 227.745  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6358.553     ± 29.056  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6560.516   ± 1449.787  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1061.108     ± 92.219  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3815.196    ± 111.977  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        832.011     ± 34.159  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6942.825   ± 1332.354  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12060.716     ± 26.010  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4536.913     ± 13.574  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3111.007     ± 39.950  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4444.683     ± 88.732  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23389.968    ± 385.542  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23579.827    ± 295.880  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     489861.829   ± 9372.927  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     502571.290   ± 5044.092  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     519411.237  ± 12078.501  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     532435.162   ± 9169.066  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-13T04:13:06Z
- **Commit:** [`743bbab`](https://github.com/prometheus/client_java/commit/743bbab03410082675b1203566ed65a84a5512d6)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 552.91M | ± 11623.35K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.74M | ± 526.16K | ops/s |
| prometheusLabelValuesInc | 114.94M | ± 2884.35K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.33M | ± 2225.48K | ops/s |
| prometheusInc | 65.86K | ± 1.19K | ops/s |
| prometheusNoLabelsInc | 56.39K | ± 1.08K | ops/s |
| prometheusAdd | 51.21K | ± 633.98 | ops/s |
| codahaleIncNoLabels | 50.01K | ± 694.42 | ops/s |
| openTelemetryBoundInc | 37.65K | ± 550.35 | ops/s |
| openTelemetryBoundAdd | 31.23K | ± 474.83 | ops/s |
| openTelemetryInc | 22.59K | ± 1.05K | ops/s |
| openTelemetryIncNoLabels | 22.54K | ± 1.21K | ops/s |
| openTelemetryAdd | 19.35K | ± 309.27 | ops/s |
| simpleclientInc | 6.57K | ± 114.03 | ops/s |
| simpleclientNoLabelsInc | 6.32K | ± 107.78 | ops/s |
| simpleclientAdd | 6.24K | ± 319.07 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.01K | ± 171.35 | ops/s |
| prometheusClassic | 4.98K | ± 1.18K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 18.81 | ops/s |
| simpleclient | 4.45K | ± 97.77 | ops/s |
| openTelemetryClassic | 4.41K | ± 393.09 | ops/s |
| openTelemetryBoundClassic | 3.56K | ± 604.30 | ops/s |
| prometheusNative | 2.89K | ± 239.99 | ops/s |
| openTelemetryBoundExponential | 1.02K | ± 98.19 | ops/s |
| openTelemetryExponential | 946.12 | ± 14.88 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.24K | ± 952.90 | ops/s |
| prometheusWriteToNull | 30.66K | ± 399.82 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 526.62K | ± 3.78K | ops/s |
| prometheusWriteToByteArray | 518.73K | ± 6.95K | ops/s |
| openMetricsWriteToByteArray | 515.35K | ± 11.57K | ops/s |
| openMetricsWriteToNull | 510.77K | ± 17.24K | ops/s |

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
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.150 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.269 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.922 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.216 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.992 | — | — |
| HistogramBenchmark.prometheusClassic | 0.770 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.668 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.291 | — | — |
| HistogramBenchmark.simpleclient | 0.211 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.113 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18477.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50009.886    ± 694.423  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19352.435    ± 309.269  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31229.810    ± 474.827  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37648.550    ± 550.347  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22593.416   ± 1049.030  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22539.766   ± 1205.711  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51206.489    ± 633.981  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  552905994.000 ± 11623348.893  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334737022.167 ± 526162.997  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65864.800   ± 1192.618  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114943638.455 ± 2884354.802  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57329617.894 ± 2225477.074  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56387.929   ± 1084.054  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6237.696    ± 319.069  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6569.151    ± 114.026  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6323.713    ± 107.783  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3561.813    ± 604.305  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1024.276     ± 98.191  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4408.021    ± 393.092  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        946.115     ± 14.876  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4981.910   ± 1182.384  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12009.799    ± 171.349  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4513.065     ± 18.810  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2892.506    ± 239.992  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4448.772     ± 97.768  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31237.239    ± 952.901  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30655.290    ± 399.816  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     515345.958  ± 11566.049  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     510772.492  ± 17236.017  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     518725.685   ± 6947.505  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     526616.116   ± 3780.127  ops/s
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

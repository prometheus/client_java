# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-23T04:24:39Z
- **Commit:** [`1469fe5`](https://github.com/prometheus/client_java/commit/1469fe5d7de1bc38f648f8bf6525d6c65af0a8b5)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 549.12M | ± 11162.42K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 333.87M | ± 349.28K | ops/s |
| prometheusLabelValuesInc | 113.69M | ± 2591.67K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.23M | ± 269.22K | ops/s |
| prometheusInc | 63.29K | ± 3.81K | ops/s |
| prometheusNoLabelsInc | 56.07K | ± 1.44K | ops/s |
| prometheusAdd | 51.42K | ± 734.87 | ops/s |
| codahaleIncNoLabels | 49.20K | ± 1.01K | ops/s |
| openTelemetryBoundInc | 37.59K | ± 610.97 | ops/s |
| openTelemetryBoundAdd | 31.75K | ± 742.65 | ops/s |
| openTelemetryInc | 23.11K | ± 317.52 | ops/s |
| openTelemetryIncNoLabels | 21.81K | ± 311.40 | ops/s |
| openTelemetryAdd | 19.38K | ± 308.09 | ops/s |
| simpleclientInc | 6.53K | ± 108.80 | ops/s |
| simpleclientAdd | 6.30K | ± 202.82 | ops/s |
| simpleclientNoLabelsInc | 6.24K | ± 114.54 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.02K | ± 161.78 | ops/s |
| prometheusClassic | 6.89K | ± 1.13K | ops/s |
| openTelemetryBoundClassic | 5.26K | ± 1.15K | ops/s |
| simpleclient | 4.45K | ± 63.71 | ops/s |
| prometheusClassicSingleThread | 4.43K | ± 51.89 | ops/s |
| openTelemetryClassic | 3.63K | ± 240.00 | ops/s |
| prometheusNative | 2.65K | ± 312.25 | ops/s |
| openTelemetryBoundExponential | 1.03K | ± 35.06 | ops/s |
| openTelemetryExponential | 991.69 | ± 15.80 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.70K | ± 827.47 | ops/s |
| prometheusWriteToNull | 30.37K | ± 1.72K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 512.39K | ± 4.33K | ops/s |
| prometheusWriteToNull | 501.94K | ± 4.78K | ops/s |
| openMetricsWriteToNull | 499.56K | ± 8.11K | ops/s |
| openMetricsWriteToByteArray | 497.64K | ± 4.22K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.150 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.188 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.913 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.262 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.950 | — | — |
| HistogramBenchmark.prometheusClassic | 0.545 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.668 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.659 | — | — |
| HistogramBenchmark.prometheusNative | 417713.416 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.111 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.116 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49198.464   ± 1006.675  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19379.307    ± 308.087  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31752.745    ± 742.652  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37586.494    ± 610.973  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23111.009    ± 317.515  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21813.092    ± 311.400  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51418.295    ± 734.868  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  549119904.635 ± 11162415.053  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  333874110.823 ± 349278.239  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63287.082   ± 3809.520  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  113687803.729 ± 2591666.016  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58228113.625 ± 269219.157  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56073.127   ± 1442.451  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6303.433    ± 202.817  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6525.029    ± 108.803  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6244.044    ± 114.539  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5261.308   ± 1149.415  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1029.668     ± 35.063  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3628.937    ± 240.001  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        991.687     ± 15.796  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6890.352   ± 1127.280  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12015.986    ± 161.777  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4429.151     ± 51.886  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2648.690    ± 312.252  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4451.247     ± 63.715  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31701.029    ± 827.470  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30372.254   ± 1717.169  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     497636.822   ± 4219.428  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     499563.683   ± 8109.081  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     512390.238   ± 4330.349  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     501943.728   ± 4778.962  ops/s
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

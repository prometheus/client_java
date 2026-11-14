# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-14T04:14:32Z
- **Commit:** [`743bbab`](https://github.com/prometheus/client_java/commit/743bbab03410082675b1203566ed65a84a5512d6)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 547.35M | ± 14142.91K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.64M | ± 917.61K | ops/s |
| prometheusLabelValuesInc | 113.88M | ± 2387.90K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.39M | ± 296.12K | ops/s |
| prometheusInc | 65.26K | ± 1.24K | ops/s |
| prometheusNoLabelsInc | 56.12K | ± 1.47K | ops/s |
| prometheusAdd | 51.07K | ± 881.53 | ops/s |
| codahaleIncNoLabels | 49.25K | ± 1.93K | ops/s |
| openTelemetryBoundInc | 37.78K | ± 653.57 | ops/s |
| openTelemetryBoundAdd | 31.52K | ± 776.84 | ops/s |
| openTelemetryInc | 23.03K | ± 414.67 | ops/s |
| openTelemetryIncNoLabels | 22.48K | ± 1.26K | ops/s |
| openTelemetryAdd | 19.21K | ± 317.52 | ops/s |
| simpleclientInc | 6.56K | ± 95.40 | ops/s |
| simpleclientAdd | 6.37K | ± 189.74 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 100.47 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 186.51 | ops/s |
| prometheusClassic | 8.16K | ± 1.44K | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 14.48 | ops/s |
| simpleclient | 4.45K | ± 77.44 | ops/s |
| openTelemetryClassic | 3.87K | ± 371.39 | ops/s |
| openTelemetryBoundClassic | 2.84K | ± 352.71 | ops/s |
| prometheusNative | 2.48K | ± 409.82 | ops/s |
| openTelemetryBoundExponential | 1.07K | ± 52.74 | ops/s |
| openTelemetryExponential | 929.95 | ± 28.38 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.05K | ± 441.05 | ops/s |
| prometheusWriteToNull | 30.54K | ± 809.95 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 507.87K | ± 4.07K | ops/s |
| prometheusWriteToByteArray | 492.17K | ± 7.34K | ops/s |
| openMetricsWriteToByteArray | 488.55K | ± 5.51K | ops/s |
| openMetricsWriteToNull | 485.97K | ± 3.25K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.333 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.884 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.248 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.007 | — | — |
| HistogramBenchmark.prometheusClassic | 0.464 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.684 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.521 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.114 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49251.568   ± 1930.818  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19206.360    ± 317.518  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31522.147    ± 776.840  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37782.131    ± 653.571  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23026.267    ± 414.669  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22482.585   ± 1261.767  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51070.975    ± 881.534  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  547348966.768 ± 14142908.254  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334640252.058 ± 917613.563  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65260.441   ± 1237.100  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  113881432.043 ± 2387904.978  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58392221.334 ± 296117.435  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56118.867   ± 1467.146  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6368.790    ± 189.740  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6560.416     ± 95.397  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6354.787    ± 100.466  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2844.203    ± 352.715  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1065.393     ± 52.745  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3874.535    ± 371.386  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        929.949     ± 28.383  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8159.782   ± 1444.510  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12001.726    ± 186.508  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4525.872     ± 14.480  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2478.176    ± 409.820  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4446.306     ± 77.440  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31050.039    ± 441.050  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30538.804    ± 809.949  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     488549.885   ± 5506.889  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485974.486   ± 3248.348  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     492167.116   ± 7339.172  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507865.304   ± 4069.844  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-28T04:17:27Z
- **Commit:** [`9458639`](https://github.com/prometheus/client_java/commit/94586397644b7152fe7b09b0b55f051075a27935)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 545.89M | ± 6105.26K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.68M | ± 258.41K | ops/s |
| prometheusLabelValuesInc | 117.43M | ± 2726.90K | ops/s |
| prometheusLabelValuesIncSingleThread | 54.79M | ± 4595.26K | ops/s |
| prometheusInc | 63.83K | ± 2.26K | ops/s |
| prometheusNoLabelsInc | 55.98K | ± 1.38K | ops/s |
| prometheusAdd | 50.91K | ± 673.34 | ops/s |
| codahaleIncNoLabels | 48.62K | ± 1.83K | ops/s |
| openTelemetryBoundInc | 37.52K | ± 711.95 | ops/s |
| openTelemetryBoundAdd | 31.99K | ± 599.51 | ops/s |
| openTelemetryIncNoLabels | 23.22K | ± 1.30K | ops/s |
| openTelemetryInc | 23.02K | ± 446.36 | ops/s |
| openTelemetryAdd | 19.34K | ± 343.44 | ops/s |
| simpleclientInc | 6.55K | ± 118.35 | ops/s |
| simpleclientNoLabelsInc | 6.48K | ± 161.99 | ops/s |
| simpleclientAdd | 6.22K | ± 320.60 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.96K | ± 192.78 | ops/s |
| prometheusClassic | 6.54K | ± 2.10K | ops/s |
| openTelemetryClassic | 5.27K | ± 629.87 | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 13.51 | ops/s |
| simpleclient | 4.42K | ± 98.96 | ops/s |
| prometheusNative | 3.07K | ± 101.11 | ops/s |
| openTelemetryBoundClassic | 3.04K | ± 286.35 | ops/s |
| openTelemetryBoundExponential | 1.05K | ± 80.89 | ops/s |
| openTelemetryExponential | 1.01K | ± 104.27 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.98K | ± 828.28 | ops/s |
| openMetricsWriteToNull | 29.83K | ± 202.15 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 524.91K | ± 7.98K | ops/s |
| prometheusWriteToByteArray | 508.72K | ± 5.21K | ops/s |
| openMetricsWriteToNull | 500.33K | ± 4.46K | ops/s |
| openMetricsWriteToByteArray | 498.09K | ± 11.43K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.144 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.310 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.896 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.183 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.937 | — | — |
| HistogramBenchmark.prometheusClassic | 0.613 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.682 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.644 | — | — |
| HistogramBenchmark.prometheusNative | 417713.205 | — | — |
| HistogramBenchmark.simpleclient | 0.214 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.118 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.114 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48623.593   ± 1833.068  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19336.373    ± 343.445  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31990.479    ± 599.514  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37523.971    ± 711.952  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23016.079    ± 446.364  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23220.168   ± 1299.139  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50914.162    ± 673.337  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  545893745.279 ± 6105255.714  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334679450.842 ± 258408.542  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63834.609   ± 2257.777  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  117432442.160 ± 2726902.499  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   54785886.856 ± 4595257.864  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55981.143   ± 1381.500  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6216.165    ± 320.596  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6552.511    ± 118.353  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6479.411    ± 161.994  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3043.660    ± 286.352  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1048.529     ± 80.887  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5268.259    ± 629.868  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1006.534    ± 104.269  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6541.336   ± 2104.251  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11964.690    ± 192.784  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4522.812     ± 13.509  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3066.831    ± 101.111  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4416.809     ± 98.961  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29829.866    ± 202.151  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30983.438    ± 828.282  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     498088.332  ± 11433.180  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     500326.610   ± 4458.983  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     508716.111   ± 5213.204  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     524906.280   ± 7982.768  ops/s
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

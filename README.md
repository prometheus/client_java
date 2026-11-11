# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-11T04:13:06Z
- **Commit:** [`25905af`](https://github.com/prometheus/client_java/commit/25905af0dbb1e3fa2465519530dd973df04a11cf)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 552.12M | ± 10629.03K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.85M | ± 405.85K | ops/s |
| prometheusLabelValuesInc | 116.27M | ± 1978.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.43M | ± 178.49K | ops/s |
| prometheusInc | 64.59K | ± 1.48K | ops/s |
| prometheusNoLabelsInc | 56.46K | ± 906.97 | ops/s |
| prometheusAdd | 51.29K | ± 841.09 | ops/s |
| codahaleIncNoLabels | 48.28K | ± 1.42K | ops/s |
| openTelemetryBoundInc | 37.71K | ± 745.87 | ops/s |
| openTelemetryBoundAdd | 31.18K | ± 596.25 | ops/s |
| openTelemetryIncNoLabels | 23.35K | ± 1.15K | ops/s |
| openTelemetryInc | 22.94K | ± 427.25 | ops/s |
| openTelemetryAdd | 19.21K | ± 320.35 | ops/s |
| simpleclientInc | 6.58K | ± 99.03 | ops/s |
| simpleclientNoLabelsInc | 6.38K | ± 100.03 | ops/s |
| simpleclientAdd | 6.32K | ± 178.83 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 194.71 | ops/s |
| prometheusClassic | 8.40K | ± 1.30K | ops/s |
| openTelemetryClassic | 4.68K | ± 1.34K | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 22.68 | ops/s |
| simpleclient | 4.44K | ± 76.46 | ops/s |
| prometheusNative | 2.90K | ± 280.87 | ops/s |
| openTelemetryBoundClassic | 2.89K | ± 932.12 | ops/s |
| openTelemetryBoundExponential | 1.07K | ± 102.68 | ops/s |
| openTelemetryExponential | 1.04K | ± 36.49 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.19K | ± 1.30K | ops/s |
| openMetricsWriteToNull | 29.03K | ± 2.68K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 507.23K | ± 6.47K | ops/s |
| openMetricsWriteToNull | 505.92K | ± 4.48K | ops/s |
| openMetricsWriteToByteArray | 498.22K | ± 3.83K | ops/s |
| prometheusWriteToByteArray | 496.41K | ± 19.90K | ops/s |

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
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.348 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.887 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.211 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.901 | — | — |
| HistogramBenchmark.prometheusClassic | 0.450 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.670 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.295 | — | — |
| HistogramBenchmark.simpleclient | 0.210 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.122 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.113 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48279.524   ± 1416.753  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19213.541    ± 320.348  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31177.919    ± 596.249  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37707.548    ± 745.867  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22943.760    ± 427.251  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23348.062   ± 1154.112  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51292.210    ± 841.087  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  552123970.697 ± 10629025.857  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334852568.935 ± 405854.715  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64585.664   ± 1478.937  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116269692.528 ± 1978754.767  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58427567.515 ± 178488.546  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56459.095    ± 906.973  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6323.574    ± 178.825  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6579.821     ± 99.025  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6379.554    ± 100.028  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2893.816    ± 932.117  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1066.816    ± 102.676  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4684.618   ± 1343.009  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1039.503     ± 36.487  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8404.931   ± 1302.453  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11996.706    ± 194.714  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4523.287     ± 22.684  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2897.768    ± 280.873  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4441.714     ± 76.464  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29030.013   ± 2682.115  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31185.943   ± 1296.086  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     498220.154   ± 3827.404  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     505918.263   ± 4475.586  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496413.167  ± 19901.855  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507228.671   ± 6465.770  ops/s
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

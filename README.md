# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-26T04:15:26Z
- **Commit:** [`ba62c2d`](https://github.com/prometheus/client_java/commit/ba62c2df3f0e4d5bb4e0323d9da2eec273feb7ec)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 289.08M | ± 7709.84K | ops/s |
| prometheusLabelValuesInc | 94.49M | ± 389.38K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.28M | ± 62.21K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.05M | ± 855.95K | ops/s |
| prometheusInc | 30.95K | ± 817.95 | ops/s |
| prometheusNoLabelsInc | 30.84K | ± 947.46 | ops/s |
| openTelemetryBoundInc | 29.24K | ± 790.84 | ops/s |
| codahaleIncNoLabels | 28.82K | ± 600.75 | ops/s |
| prometheusAdd | 28.38K | ± 247.59 | ops/s |
| openTelemetryBoundAdd | 27.35K | ± 108.98 | ops/s |
| openTelemetryIncNoLabels | 23.55K | ± 886.26 | ops/s |
| openTelemetryInc | 20.67K | ± 1.20K | ops/s |
| openTelemetryAdd | 19.31K | ± 447.76 | ops/s |
| simpleclientInc | 6.48K | ± 212.83 | ops/s |
| simpleclientAdd | 6.20K | ± 83.63 | ops/s |
| simpleclientNoLabelsInc | 6.05K | ± 68.34 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.87K | ± 29.56 | ops/s |
| simpleclient | 4.16K | ± 126.80 | ops/s |
| prometheusClassic | 3.88K | ± 943.82 | ops/s |
| openTelemetryBoundClassic | 3.33K | ± 1.25K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 80.36 | ops/s |
| openTelemetryClassic | 3.13K | ± 304.32 | ops/s |
| prometheusNative | 2.27K | ± 64.67 | ops/s |
| openTelemetryBoundExponential | 616.32 | ± 131.14 | ops/s |
| openTelemetryExponential | 556.64 | ± 16.55 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 19.87K | ± 38.60 | ops/s |
| prometheusWriteToNull | 19.76K | ± 128.08 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 341.21K | ± 6.17K | ops/s |
| prometheusWriteToByteArray | 337.09K | ± 4.62K | ops/s |
| openMetricsWriteToNull | 317.99K | ± 1.46K | ops/s |
| openMetricsWriteToByteArray | 315.81K | ± 2.23K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.032 | — | — |
| CounterBenchmark.openTelemetryInc | 0.045 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.130 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.119 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.120 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.154 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.307 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.573 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.304 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.685 | — | — |
| HistogramBenchmark.prometheusClassic | 0.986 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.028 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.855 | — | — |
| HistogramBenchmark.prometheusNative | 417713.660 | — | — |
| HistogramBenchmark.simpleclient | 0.226 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.177 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.178 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18477.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.669 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28821.469    ± 600.753  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19308.036    ± 447.761  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27350.387    ± 108.984  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      29240.122    ± 790.841  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      20672.789   ± 1197.632  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23546.270    ± 886.260  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28379.949    ± 247.586  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  289078112.275 ± 7709837.661  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93280061.301  ± 62206.693  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30949.327    ± 817.951  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   94492712.240 ± 389377.839  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57045870.993 ± 855946.378  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30837.627    ± 947.460  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6202.947     ± 83.633  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6476.402    ± 212.826  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6047.031     ± 68.336  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3326.511   ± 1247.520  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        616.324    ± 131.143  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3127.387    ± 304.323  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        556.644     ± 16.548  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3879.861    ± 943.818  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7872.463     ± 29.562  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3230.642     ± 80.359  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2268.356     ± 64.671  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4163.959    ± 126.799  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      19865.314     ± 38.598  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      19763.432    ± 128.083  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     315813.271   ± 2234.996  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     317985.872   ± 1463.989  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     337094.273   ± 4615.962  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     341209.479   ± 6169.865  ops/s
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

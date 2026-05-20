# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-20T04:37:51Z
- **Commit:** [`e11ce3d`](https://github.com/prometheus/client_java/commit/e11ce3de19daf5acd2f73ffb90c96689c172f3c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.63K | ± 471.86 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.46K | ± 978.96 | ops/s | 1.2x slower |
| prometheusAdd | 49.36K | ± 744.07 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.40K | ± 1.16K | ops/s | 1.4x slower |
| simpleclientInc | 6.10K | ± 96.09 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.90K | ± 129.68 | ops/s | 10x slower |
| simpleclientAdd | 5.81K | ± 13.15 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.41K | ± 1.40K | ops/s | 11x slower |
| openTelemetryInc | 4.80K | ± 1.04K | ops/s | 12x slower |
| openTelemetryAdd | 4.49K | ± 858.23 | ops/s | 13x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.61K | ± 879.89 | ops/s | **fastest** |
| simpleclient | 4.39K | ± 30.00 | ops/s | 1.0x slower |
| prometheusNative | 3.00K | ± 251.93 | ops/s | 1.5x slower |
| openTelemetryClassic | 718.82 | ± 8.95 | ops/s | 6.4x slower |
| openTelemetryExponential | 576.60 | ± 25.70 | ops/s | 8.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.69K | ± 143.98 | ops/s | **fastest** |
| openMetricsWriteToNull | 26.93K | ± 633.89 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 577.99K | ± 4.54K | ops/s | **fastest** |
| prometheusWriteToByteArray | 571.53K | ± 2.74K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 541.68K | ± 9.59K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 536.65K | ± 4.10K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42395.804   ± 1162.257  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4488.992    ± 858.228  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4795.052   ± 1043.165  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5408.290   ± 1396.982  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      49357.904    ± 744.067  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59629.769    ± 471.864  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51458.021    ± 978.957  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5813.896     ± 13.147  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6102.689     ± 96.092  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5901.036    ± 129.676  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        718.823      ± 8.952  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        576.602     ± 25.697  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4608.093    ± 879.892  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2998.907    ± 251.933  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4392.789     ± 29.995  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      26933.354    ± 633.886  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27686.006    ± 143.977  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     536653.705   ± 4100.522  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     541680.088   ± 9594.878  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     571530.657   ± 2743.856  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     577991.409   ± 4537.964  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

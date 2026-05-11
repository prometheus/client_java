# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-11T04:39:06Z
- **Commit:** [`11cb921`](https://github.com/prometheus/client_java/commit/11cb921cdea4789cf86ca903867ce9e3e5debe9e)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.15K | ± 1.12K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.63K | ± 558.26 | ops/s | 1.2x slower |
| prometheusAdd | 51.65K | ± 96.02 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.25K | ± 1.85K | ops/s | 1.3x slower |
| simpleclientInc | 6.52K | ± 99.81 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.37K | ± 21.83 | ops/s | 10x slower |
| simpleclientAdd | 6.36K | ± 202.65 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.48K | ± 182.57 | ops/s | 19x slower |
| openTelemetryAdd | 3.19K | ± 420.37 | ops/s | 20x slower |
| openTelemetryInc | 3.13K | ± 435.63 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.50K | ± 1.65K | ops/s | **fastest** |
| simpleclient | 4.44K | ± 82.74 | ops/s | 1.2x slower |
| prometheusNative | 2.93K | ± 385.51 | ops/s | 1.9x slower |
| openTelemetryClassic | 735.54 | ± 15.59 | ops/s | 7.5x slower |
| openTelemetryExponential | 576.67 | ± 11.26 | ops/s | 9.5x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.98K | ± 436.09 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.63K | ± 287.80 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 511.31K | ± 6.36K | ops/s | **fastest** |
| prometheusWriteToByteArray | 506.94K | ± 3.43K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 492.91K | ± 2.01K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 486.11K | ± 3.19K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49246.277   ± 1846.830  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3192.220    ± 420.371  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3125.519    ± 435.633  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3480.966    ± 182.566  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51646.586     ± 96.022  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65152.116   ± 1123.990  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56625.379    ± 558.258  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6364.873    ± 202.650  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6519.376     ± 99.805  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6374.606     ± 21.831  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        735.535     ± 15.591  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        576.673     ± 11.262  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5504.163   ± 1653.578  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2926.431    ± 385.505  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4438.090     ± 82.740  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23633.343    ± 287.803  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23980.897    ± 436.089  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     486107.214   ± 3189.237  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     492910.716   ± 2007.273  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     506941.569   ± 3428.263  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     511308.091   ± 6355.290  ops/s
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

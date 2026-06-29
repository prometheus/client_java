# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-29T04:40:51Z
- **Commit:** [`2a2c73d`](https://github.com/prometheus/client_java/commit/2a2c73d7d23bfa291b10df85056027398e8a868d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.77K | ± 194.99 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.76K | ± 506.69 | ops/s | 1.2x slower |
| prometheusAdd | 50.68K | ± 844.22 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.03K | ± 1.14K | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 38.10 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.38K | ± 33.04 | ops/s | 10x slower |
| simpleclientAdd | 6.36K | ± 141.27 | ops/s | 10x slower |
| openTelemetryInc | 3.73K | ± 309.69 | ops/s | 18x slower |
| openTelemetryAdd | 3.54K | ± 421.85 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.11K | ± 424.89 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.89K | ± 696.72 | ops/s | **fastest** |
| simpleclient | 4.43K | ± 59.81 | ops/s | 1.1x slower |
| prometheusNative | 3.20K | ± 34.71 | ops/s | 1.5x slower |
| openTelemetryClassic | 741.23 | ± 29.22 | ops/s | 6.6x slower |
| openTelemetryExponential | 711.31 | ± 60.18 | ops/s | 6.9x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.61K | ± 465.86 | ops/s | **fastest** |
| prometheusWriteToNull | 24.16K | ± 780.50 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 508.53K | ± 10.52K | ops/s | **fastest** |
| openMetricsWriteToByteArray | 490.34K | ± 2.84K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 481.98K | ± 7.54K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 475.08K | ± 7.88K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49030.352   ± 1140.294  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3537.082    ± 421.851  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3731.215    ± 309.687  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3107.388    ± 424.892  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50681.469    ± 844.224  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65765.155    ± 194.994  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56759.137    ± 506.693  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6359.845    ± 141.273  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6560.037     ± 38.096  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6378.859     ± 33.039  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        741.232     ± 29.223  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        711.315     ± 60.176  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4893.824    ± 696.715  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3202.454     ± 34.706  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4427.313     ± 59.806  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24607.138    ± 465.864  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24163.541    ± 780.500  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     490336.474   ± 2838.626  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     475075.027   ± 7878.366  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     481983.481   ± 7539.820  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     508529.760  ± 10524.022  ops/s
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

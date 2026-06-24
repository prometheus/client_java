# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-24T04:37:31Z
- **Commit:** [`a017f80`](https://github.com/prometheus/client_java/commit/a017f80980d91a5fa8ffe930c820f836c3d1b2ff)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.07K | ± 1.10K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.98K | ± 428.27 | ops/s | 1.1x slower |
| prometheusAdd | 48.60K | ± 3.67K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.74K | ± 1.07K | ops/s | 1.4x slower |
| simpleclientInc | 6.55K | ± 23.65 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.35K | ± 42.03 | ops/s | 10x slower |
| simpleclientAdd | 6.19K | ± 199.57 | ops/s | 11x slower |
| openTelemetryInc | 3.31K | ± 481.09 | ops/s | 20x slower |
| openTelemetryAdd | 3.04K | ± 77.10 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 2.94K | ± 211.90 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.25K | ± 1.42K | ops/s | **fastest** |
| simpleclient | 4.43K | ± 46.72 | ops/s | 1.2x slower |
| prometheusNative | 2.65K | ± 59.95 | ops/s | 2.0x slower |
| openTelemetryClassic | 745.83 | ± 9.29 | ops/s | 7.0x slower |
| openTelemetryExponential | 635.21 | ± 74.75 | ops/s | 8.3x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.92K | ± 622.61 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.11K | ± 935.98 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 515.59K | ± 3.26K | ops/s | **fastest** |
| prometheusWriteToByteArray | 504.08K | ± 4.30K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.54K | ± 1.80K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 481.09K | ± 3.25K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47738.094   ± 1073.017  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3041.147     ± 77.104  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3305.505    ± 481.086  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2943.971    ± 211.897  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48597.149   ± 3672.402  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65072.582   ± 1102.043  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56976.907    ± 428.266  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6193.004    ± 199.570  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6551.859     ± 23.646  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6349.287     ± 42.034  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        745.828      ± 9.287  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        635.205     ± 74.753  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5246.210   ± 1419.423  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2647.670     ± 59.949  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4426.480     ± 46.718  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23111.651    ± 935.983  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23924.845    ± 622.610  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481093.902   ± 3251.977  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487537.475   ± 1804.758  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     504079.878   ± 4296.922  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     515592.439   ± 3257.804  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-12T04:25:07Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 63.37K | ± 3.85K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.89K | ± 6.67K | ops/s | 1.2x slower |
| prometheusAdd | 51.21K | ± 198.03 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 47.61K | ± 817.63 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.30K | ± 372.52 | ops/s | 3.5x slower |
| openTelemetryInc | 14.96K | ± 324.20 | ops/s | 4.2x slower |
| openTelemetryAdd | 13.05K | ± 27.09 | ops/s | 4.9x slower |
| simpleclientInc | 6.60K | ± 31.75 | ops/s | 9.6x slower |
| simpleclientAdd | 6.32K | ± 229.81 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.31K | ± 106.96 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.17K | ± 259.14 | ops/s | **fastest** |
| prometheusClassic | 5.78K | ± 1.41K | ops/s | 2.1x slower |
| prometheusClassicSingleThread | 4.57K | ± 29.74 | ops/s | 2.7x slower |
| simpleclient | 4.39K | ± 27.57 | ops/s | 2.8x slower |
| prometheusNative | 2.77K | ± 60.49 | ops/s | 4.4x slower |
| openTelemetryClassic | 796.12 | ± 59.56 | ops/s | 15x slower |
| openTelemetryExponential | 760.27 | ± 158.26 | ops/s | 16x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.57K | ± 998.72 | ops/s | **fastest** |
| prometheusWriteToNull | 23.34K | ± 897.79 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 511.70K | ± 2.98K | ops/s | **fastest** |
| prometheusWriteToByteArray | 495.33K | ± 4.30K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.84K | ± 2.90K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 480.74K | ± 7.88K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47608.388    ± 817.630  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      13054.558     ± 27.087  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14964.639    ± 324.196  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18301.686    ± 372.524  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51206.974    ± 198.029  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63374.524   ± 3847.182  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51888.546   ± 6668.631  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6319.253    ± 229.808  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6600.747     ± 31.751  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6305.610    ± 106.959  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        796.120     ± 59.555  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        760.267    ± 158.262  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5783.369   ± 1405.989  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12167.169    ± 259.139  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4565.098     ± 29.737  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2772.850     ± 60.488  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4388.386     ± 27.570  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23569.820    ± 998.718  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23339.760    ± 897.792  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480735.307   ± 7881.321  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484843.788   ± 2898.988  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     495327.344   ± 4303.224  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     511696.649   ± 2983.755  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

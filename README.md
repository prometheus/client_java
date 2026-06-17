# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-17T04:39:44Z
- **Commit:** [`e550766`](https://github.com/prometheus/client_java/commit/e550766096ab9986f47767a1609e73220e10967a)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 58.11K | ± 2.39K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.58K | ± 507.01 | ops/s | 1.1x slower |
| prometheusAdd | 48.70K | ± 871.43 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.12K | ± 169.85 | ops/s | 1.3x slower |
| simpleclientInc | 6.09K | ± 11.23 | ops/s | 9.5x slower |
| simpleclientAdd | 6.03K | ± 178.84 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 5.71K | ± 81.67 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.48K | ± 1.28K | ops/s | 11x slower |
| openTelemetryInc | 5.23K | ± 1.11K | ops/s | 11x slower |
| openTelemetryAdd | 3.29K | ± 246.64 | ops/s | 18x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 8.50K | ± 2.06K | ops/s | **fastest** |
| simpleclient | 4.33K | ± 50.98 | ops/s | 2.0x slower |
| prometheusNative | 3.02K | ± 226.10 | ops/s | 2.8x slower |
| openTelemetryClassic | 746.01 | ± 35.71 | ops/s | 11x slower |
| openTelemetryExponential | 572.01 | ± 5.87 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.43K | ± 328.45 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.42K | ± 269.59 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 566.66K | ± 3.15K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.62K | ± 5.57K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 523.52K | ± 11.87K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 519.41K | ± 2.62K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44116.070    ± 169.848  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3286.889    ± 246.641  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5233.479   ± 1114.519  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5479.017   ± 1279.290  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48700.902    ± 871.427  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58105.103   ± 2388.766  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51583.656    ± 507.014  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6026.443    ± 178.842  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6089.144     ± 11.229  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5712.887     ± 81.670  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        746.010     ± 35.714  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        572.008      ± 5.868  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8502.860   ± 2058.522  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3021.499    ± 226.098  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4334.560     ± 50.983  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27422.155    ± 269.589  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27426.829    ± 328.449  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     519414.098   ± 2621.050  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     523518.158  ± 11870.734  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547619.794   ± 5569.259  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     566660.395   ± 3146.216  ops/s
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

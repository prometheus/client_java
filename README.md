# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-07T04:33:00Z
- **Commit:** [`c29367d`](https://github.com/prometheus/client_java/commit/c29367daf4e6aec49eecf321b8e41553c2e194d5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.92K | ± 363.74 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.35K | ± 1.15K | ops/s | 1.2x slower |
| prometheusAdd | 50.75K | ± 485.32 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 42.37K | ± 6.41K | ops/s | 1.6x slower |
| simpleclientInc | 6.56K | ± 9.19 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 30.96 | ops/s | 10x slower |
| simpleclientAdd | 6.09K | ± 289.70 | ops/s | 11x slower |
| openTelemetryAdd | 3.35K | ± 297.01 | ops/s | 20x slower |
| openTelemetryInc | 3.10K | ± 423.69 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 3.06K | ± 143.87 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.06K | ± 1.51K | ops/s | **fastest** |
| simpleclient | 4.35K | ± 43.07 | ops/s | 1.2x slower |
| prometheusNative | 2.84K | ± 239.75 | ops/s | 1.8x slower |
| openTelemetryClassic | 776.98 | ± 18.28 | ops/s | 6.5x slower |
| openTelemetryExponential | 578.40 | ± 25.41 | ops/s | 8.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.62K | ± 824.22 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.39K | ± 381.33 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 483.31K | ± 11.57K | ops/s | **fastest** |
| prometheusWriteToByteArray | 474.61K | ± 9.09K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 465.46K | ± 4.67K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 456.34K | ± 7.12K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42374.471   ± 6405.323  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3351.736    ± 297.013  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3097.861    ± 423.694  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3055.023    ± 143.867  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50751.581    ± 485.317  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65916.291    ± 363.739  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56348.162   ± 1153.795  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6085.312    ± 289.698  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6559.515      ± 9.189  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6357.676     ± 30.963  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        776.977     ± 18.277  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        578.403     ± 25.410  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5055.200   ± 1509.114  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2840.290    ± 239.746  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4350.169     ± 43.068  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23390.033    ± 381.327  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23620.547    ± 824.219  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     456335.699   ± 7116.404  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     465463.192   ± 4668.503  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     474611.915   ± 9088.926  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     483312.984  ± 11574.363  ops/s
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

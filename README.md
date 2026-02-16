# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-16T04:28:56Z
- **Commit:** [`05ad751`](https://github.com/prometheus/client_java/commit/05ad751a40053f11eae90b9e6cbd741814ca71a7)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 61.87K | ± 2.86K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.32K | ± 1.32K | ops/s | 1.1x slower |
| prometheusAdd | 51.35K | ± 277.19 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.83K | ± 1.51K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.57K | ± 208.57 | ops/s | 9.4x slower |
| simpleclientInc | 6.54K | ± 327.37 | ops/s | 9.5x slower |
| simpleclientAdd | 6.44K | ± 192.22 | ops/s | 9.6x slower |
| openTelemetryIncNoLabels | 1.42K | ± 189.15 | ops/s | 43x slower |
| openTelemetryInc | 1.40K | ± 87.51 | ops/s | 44x slower |
| openTelemetryAdd | 1.38K | ± 56.22 | ops/s | 45x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.57K | ± 21.11 | ops/s | **fastest** |
| prometheusClassic | 4.31K | ± 315.19 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 260.08 | ops/s | 1.5x slower |
| openTelemetryClassic | 656.81 | ± 38.17 | ops/s | 7.0x slower |
| openTelemetryExponential | 549.90 | ± 23.40 | ops/s | 8.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 484.10K | ± 3.19K | ops/s | **fastest** |
| prometheusWriteToByteArray | 479.43K | ± 6.27K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 461.92K | ± 4.41K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 461.86K | ± 11.70K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48826.997   ± 1513.933  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1377.249     ± 56.224  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1397.546     ± 87.507  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1423.218    ± 189.146  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51350.476    ± 277.195  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      61867.566   ± 2862.830  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56318.854   ± 1319.279  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6436.546    ± 192.223  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6535.634    ± 327.373  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6569.634    ± 208.569  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        656.809     ± 38.174  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        549.896     ± 23.399  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4314.201    ± 315.188  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3062.096    ± 260.077  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4570.897     ± 21.114  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     461863.751  ± 11697.140  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     461921.061   ± 4411.548  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     479429.744   ± 6272.687  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     484100.923   ± 3187.435  ops/s
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

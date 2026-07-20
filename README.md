# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-20T04:31:47Z
- **Commit:** [`9392302`](https://github.com/prometheus/client_java/commit/93923020cf663a047a2fd7fe20868df16157571b)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.72K | ± 570.26 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.69K | ± 448.92 | ops/s | 1.2x slower |
| prometheusAdd | 51.13K | ± 644.15 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.03K | ± 1.69K | ops/s | 1.3x slower |
| simpleclientInc | 6.57K | ± 29.43 | ops/s | 10.0x slower |
| simpleclientAdd | 6.47K | ± 10.83 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 23.67 | ops/s | 10x slower |
| openTelemetryAdd | 3.44K | ± 199.11 | ops/s | 19x slower |
| openTelemetryInc | 3.25K | ± 271.37 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.15K | ± 109.93 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 6.21K | ± 2.52K | ops/s | **fastest** |
| simpleclient | 4.50K | ± 32.83 | ops/s | 1.4x slower |
| prometheusNative | 3.13K | ± 138.20 | ops/s | 2.0x slower |
| openTelemetryClassic | 776.28 | ± 19.44 | ops/s | 8.0x slower |
| openTelemetryExponential | 626.94 | ± 33.42 | ops/s | 9.9x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.57K | ± 1.06K | ops/s | **fastest** |
| prometheusWriteToNull | 22.93K | ± 534.95 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToByteArray | 497.56K | ± 14.66K | ops/s | **fastest** |
| prometheusWriteToNull | 491.32K | ± 8.25K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 476.61K | ± 6.13K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.52K | ± 11.34K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49034.673   ± 1689.791  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3441.566    ± 199.113  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3250.261    ± 271.367  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3154.783    ± 109.931  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51127.266    ± 644.150  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65718.969    ± 570.260  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56690.126    ± 448.922  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6466.381     ± 10.829  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6573.056     ± 29.431  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6361.102     ± 23.669  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        776.281     ± 19.445  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        626.940     ± 33.421  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6205.103   ± 2516.044  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3125.032    ± 138.196  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4495.946     ± 32.830  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23573.240   ± 1058.015  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22932.510    ± 534.947  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475523.268  ± 11341.126  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     476613.132   ± 6134.900  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497555.567  ± 14655.113  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491322.964   ± 8246.596  ops/s
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

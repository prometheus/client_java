# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-19T04:30:17Z
- **Commit:** [`8d91443`](https://github.com/prometheus/client_java/commit/8d91443665952d8a2585a9e2f220a5811ef2a051)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.74K | ± 68.17 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.16K | ± 115.82 | ops/s | 1.1x slower |
| prometheusAdd | 51.14K | ± 759.41 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.90K | ± 8.21K | ops/s | 1.5x slower |
| simpleclientInc | 6.56K | ± 49.80 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 23.49 | ops/s | 10x slower |
| simpleclientAdd | 6.09K | ± 341.51 | ops/s | 11x slower |
| openTelemetryInc | 3.33K | ± 382.92 | ops/s | 20x slower |
| openTelemetryAdd | 3.28K | ± 209.80 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.21K | ± 247.13 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 5.33K | ± 398.71 | ops/s | **fastest** |
| simpleclient | 4.33K | ± 157.38 | ops/s | 1.2x slower |
| prometheusNative | 2.77K | ± 246.10 | ops/s | 1.9x slower |
| openTelemetryClassic | 737.40 | ± 8.85 | ops/s | 7.2x slower |
| openTelemetryExponential | 596.76 | ± 52.35 | ops/s | 8.9x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.20K | ± 792.37 | ops/s | **fastest** |
| openMetricsWriteToNull | 22.47K | ± 567.20 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 504.74K | ± 2.70K | ops/s | **fastest** |
| prometheusWriteToByteArray | 491.10K | ± 3.81K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 480.96K | ± 6.02K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.64K | ± 3.12K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44898.589   ± 8213.985  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3282.393    ± 209.803  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3330.551    ± 382.920  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3205.376    ± 247.132  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51141.834    ± 759.411  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65738.006     ± 68.172  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57164.742    ± 115.818  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6087.115    ± 341.512  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6557.024     ± 49.801  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6357.178     ± 23.492  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        737.402      ± 8.855  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        596.758     ± 52.347  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5327.046    ± 398.708  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2772.428    ± 246.104  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4329.461    ± 157.376  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      22469.926    ± 567.204  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23199.111    ± 792.372  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475638.081   ± 3118.689  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     480956.263   ± 6016.381  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491100.685   ± 3806.070  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     504743.202   ± 2698.927  ops/s
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

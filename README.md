# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-08T04:09:41Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusNoLabelsInc | 30.17K | ± 264.13 | ops/s | **fastest** |
| prometheusInc | 30.04K | ± 371.54 | ops/s | 1.0x slower |
| codahaleIncNoLabels | 29.71K | ± 1.70K | ops/s | 1.0x slower |
| prometheusAdd | 29.16K | ± 461.46 | ops/s | 1.0x slower |
| openTelemetryIncNoLabels | 19.45K | ± 169.13 | ops/s | 1.6x slower |
| openTelemetryInc | 17.10K | ± 210.69 | ops/s | 1.8x slower |
| openTelemetryAdd | 14.83K | ± 126.22 | ops/s | 2.0x slower |
| simpleclientInc | 7.64K | ± 54.27 | ops/s | 4.0x slower |
| simpleclientNoLabelsInc | 7.51K | ± 55.62 | ops/s | 4.0x slower |
| simpleclientAdd | 7.37K | ± 74.75 | ops/s | 4.1x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 7.80K | ± 47.58 | ops/s | **fastest** |
| simpleclient | 4.90K | ± 59.94 | ops/s | 1.6x slower |
| prometheusClassicSingleThread | 3.30K | ± 89.93 | ops/s | 2.4x slower |
| prometheusClassic | 2.66K | ± 432.87 | ops/s | 2.9x slower |
| prometheusNative | 2.41K | ± 38.51 | ops/s | 3.2x slower |
| openTelemetryExponential | 485.28 | ± 66.94 | ops/s | 16x slower |
| openTelemetryClassic | 476.12 | ± 24.91 | ops/s | 16x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 20.10K | ± 323.11 | ops/s | **fastest** |
| prometheusWriteToNull | 20.10K | ± 123.13 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 324.29K | ± 3.96K | ops/s | **fastest** |
| prometheusWriteToByteArray | 320.46K | ± 2.76K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 297.92K | ± 4.43K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 297.82K | ± 3.09K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29709.162   ± 1701.402  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      14833.809    ± 126.222  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17104.619    ± 210.695  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      19447.800    ± 169.132  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      29162.140    ± 461.460  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30042.712    ± 371.537  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30165.041    ± 264.134  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7366.751     ± 74.746  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7636.025     ± 54.269  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7510.326     ± 55.618  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        476.124     ± 24.908  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        485.277     ± 66.943  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2655.298    ± 432.866  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7801.059     ± 47.581  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3300.046     ± 89.933  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2411.891     ± 38.513  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4897.815     ± 59.945  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      20103.364    ± 323.108  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      20097.638    ± 123.132  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     297918.236   ± 4432.066  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     297815.281   ± 3092.481  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     320457.235   ± 2763.128  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     324290.873   ± 3958.396  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-15T03:52:57Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.25K | ± 695.02 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.88K | ± 318.65 | ops/s | 1.2x slower |
| prometheusAdd | 51.14K | ± 739.29 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.49K | ± 345.87 | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.14K | ± 317.93 | ops/s | 3.7x slower |
| openTelemetryInc | 14.98K | ± 388.39 | ops/s | 4.4x slower |
| openTelemetryAdd | 13.00K | ± 56.44 | ops/s | 5.1x slower |
| simpleclientInc | 6.55K | ± 45.08 | ops/s | 10x slower |
| simpleclientAdd | 6.45K | ± 24.37 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.28K | ± 103.50 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.28K | ± 39.78 | ops/s | **fastest** |
| prometheusClassic | 5.23K | ± 1.48K | ops/s | 2.3x slower |
| prometheusClassicSingleThread | 4.60K | ± 54.45 | ops/s | 2.7x slower |
| simpleclient | 4.36K | ± 34.70 | ops/s | 2.8x slower |
| prometheusNative | 2.76K | ± 377.04 | ops/s | 4.4x slower |
| openTelemetryExponential | 923.11 | ± 94.49 | ops/s | 13x slower |
| openTelemetryClassic | 798.83 | ± 19.56 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.11K | ± 440.97 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.69K | ± 768.46 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 509.13K | ± 4.24K | ops/s | **fastest** |
| prometheusWriteToByteArray | 503.40K | ± 3.21K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.23K | ± 1.11K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 482.88K | ± 2.27K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47490.272    ± 345.866  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12997.538     ± 56.439  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14978.665    ± 388.386  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18140.818    ± 317.929  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51136.438    ± 739.292  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66247.306    ± 695.017  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56875.479    ± 318.655  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6447.044     ± 24.366  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6549.598     ± 45.078  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6279.708    ± 103.498  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        798.832     ± 19.556  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        923.112     ± 94.487  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5232.987   ± 1483.848  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12276.966     ± 39.782  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4604.001     ± 54.447  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2763.614    ± 377.040  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4360.251     ± 34.696  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23688.144    ± 768.463  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24113.713    ± 440.974  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     482880.108   ± 2266.544  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487225.994   ± 1111.484  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     503399.151   ± 3205.827  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509132.785   ± 4235.088  ops/s
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

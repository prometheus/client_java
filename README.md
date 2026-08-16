# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-16T03:58:29Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusNoLabelsInc | 26.60K | ± 115.70 | ops/s | **fastest** |
| codahaleIncNoLabels | 26.47K | ± 1.26K | ops/s | 1.0x slower |
| prometheusInc | 26.42K | ± 168.94 | ops/s | 1.0x slower |
| prometheusAdd | 25.83K | ± 115.30 | ops/s | 1.0x slower |
| openTelemetryIncNoLabels | 16.86K | ± 211.57 | ops/s | 1.6x slower |
| openTelemetryInc | 15.18K | ± 115.47 | ops/s | 1.8x slower |
| openTelemetryAdd | 13.22K | ± 56.81 | ops/s | 2.0x slower |
| simpleclientInc | 6.69K | ± 123.44 | ops/s | 4.0x slower |
| simpleclientNoLabelsInc | 6.54K | ± 109.29 | ops/s | 4.1x slower |
| simpleclientAdd | 6.46K | ± 89.41 | ops/s | 4.1x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 6.82K | ± 34.65 | ops/s | **fastest** |
| simpleclient | 4.43K | ± 23.39 | ops/s | 1.5x slower |
| prometheusClassicSingleThread | 2.91K | ± 90.48 | ops/s | 2.3x slower |
| prometheusClassic | 2.52K | ± 426.10 | ops/s | 2.7x slower |
| prometheusNative | 2.24K | ± 246.90 | ops/s | 3.0x slower |
| openTelemetryClassic | 487.06 | ± 44.74 | ops/s | 14x slower |
| openTelemetryExponential | 458.54 | ± 29.42 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 17.91K | ± 24.07 | ops/s | **fastest** |
| prometheusWriteToNull | 17.90K | ± 32.93 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 301.76K | ± 1.11K | ops/s | **fastest** |
| prometheusWriteToByteArray | 300.02K | ± 1.55K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 282.75K | ± 1.45K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 282.43K | ± 685.44 | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      26472.372   ± 1264.908  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      13216.392     ± 56.813  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15180.202    ± 115.467  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      16855.826    ± 211.572  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      25833.867    ± 115.295  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      26420.814    ± 168.938  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      26599.419    ± 115.701  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6456.674     ± 89.414  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6691.331    ± 123.445  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6536.220    ± 109.285  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        487.056     ± 44.738  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        458.541     ± 29.422  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2520.871    ± 426.102  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6818.930     ± 34.647  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2911.283     ± 90.475  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2239.113    ± 246.895  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4434.119     ± 23.390  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17906.062     ± 24.070  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17904.805     ± 32.935  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     282752.532   ± 1446.854  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     282428.905    ± 685.437  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     300019.062   ± 1546.238  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     301759.910   ± 1112.649  ops/s
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

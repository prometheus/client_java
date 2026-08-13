# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-13T04:26:31Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 58.99K | ± 1.48K | ops/s | **fastest** |
| prometheusAdd | 48.82K | ± 611.27 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.19K | ± 338.70 | ops/s | 1.3x slower |
| prometheusNoLabelsInc | 42.12K | ± 13.53K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 17.15K | ± 17.05 | ops/s | 3.4x slower |
| openTelemetryInc | 13.88K | ± 33.67 | ops/s | 4.2x slower |
| openTelemetryAdd | 12.04K | ± 137.14 | ops/s | 4.9x slower |
| simpleclientInc | 6.20K | ± 3.19 | ops/s | 9.5x slower |
| simpleclientAdd | 6.04K | ± 166.61 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.88K | ± 14.74 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 13.98K | ± 109.76 | ops/s | **fastest** |
| prometheusClassic | 5.81K | ± 1.32K | ops/s | 2.4x slower |
| prometheusClassicSingleThread | 5.18K | ± 1.15K | ops/s | 2.7x slower |
| simpleclient | 4.51K | ± 51.64 | ops/s | 3.1x slower |
| prometheusNative | 3.05K | ± 270.03 | ops/s | 4.6x slower |
| openTelemetryClassic | 863.43 | ± 72.05 | ops/s | 16x slower |
| openTelemetryExponential | 741.17 | ± 33.57 | ops/s | 19x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 27.74K | ± 104.59 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.18K | ± 446.78 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 579.51K | ± 6.26K | ops/s | **fastest** |
| prometheusWriteToByteArray | 559.73K | ± 8.05K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 546.89K | ± 2.93K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 535.82K | ± 4.11K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44185.799    ± 338.701  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12037.081    ± 137.136  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13883.500     ± 33.673  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17147.042     ± 17.046  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48822.812    ± 611.272  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58992.335   ± 1480.955  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      42124.985  ± 13533.133  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6036.326    ± 166.611  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6202.522      ± 3.192  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5877.887     ± 14.738  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        863.435     ± 72.046  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        741.174     ± 33.566  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5807.233   ± 1320.752  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13976.250    ± 109.759  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5184.650   ± 1154.328  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3050.164    ± 270.025  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4508.029     ± 51.636  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27176.096    ± 446.776  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27741.417    ± 104.593  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     535815.507   ± 4108.172  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     546892.106   ± 2934.009  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     559726.328   ± 8048.987  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     579514.007   ± 6261.960  ops/s
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
